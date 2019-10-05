package org.thoughtcrime.securesms;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.b44t.messenger.DcChat;
import com.b44t.messenger.DcContact;

import org.thoughtcrime.securesms.components.AvatarImageView;
import org.thoughtcrime.securesms.connect.DcHelper;
import org.thoughtcrime.securesms.mms.GlideRequests;
import org.thoughtcrime.securesms.util.Prefs;
import org.thoughtcrime.securesms.util.Util;
import org.thoughtcrime.securesms.util.ViewUtil;

public class ConversationTitleView extends RelativeLayout {

  @SuppressWarnings("unused")
  private static final String TAG = ConversationTitleView.class.getSimpleName();

  private View            content;
  private ImageView       back;
  private AvatarImageView avatar;
  private TextView        title;
  private TextView        subtitle;

  public ConversationTitleView(Context context) {
    this(context, null);
  }

  public ConversationTitleView(Context context, AttributeSet attrs) {
    super(context, attrs);

  }

  @Override
  public void onFinishInflate() {
    super.onFinishInflate();

    this.back     = ViewUtil.findById(this, R.id.up_button);
    this.content  = ViewUtil.findById(this, R.id.content);
    this.title    = ViewUtil.findById(this, R.id.title);
    this.subtitle = ViewUtil.findById(this, R.id.subtitle);
    this.avatar   = ViewUtil.findById(this, R.id.contact_photo_image);

    ViewUtil.setTextViewGravityStart(this.title, getContext());
    ViewUtil.setTextViewGravityStart(this.subtitle, getContext());
  }

  public void setTitle(@NonNull GlideRequests glideRequests, @Nullable DcChat dcChat) {
    setTitle(glideRequests, dcChat, true);
  }

  public void setTitle(@NonNull GlideRequests glideRequests, @Nullable DcChat dcChat, boolean showSubtitle) {

    int imgLeft = 0;
    int imgRight = 0;

    if (dcChat == null) {
      setComposeTitle();
    } else {
      setRecipientTitle(dcChat, showSubtitle);
      if (Prefs.isChatMuted(getContext(), dcChat.getId())) {
        imgLeft = R.drawable.ic_volume_off_white_18dp;
      }
      if (dcChat.isVerified()) {
        imgRight = R.drawable.ic_verified;
      }
      this.avatar.setAvatar(glideRequests, DcHelper.getContext(getContext()).getRecipient(dcChat), false);
    }

    title.setCompoundDrawablesWithIntrinsicBounds(imgLeft, 0, imgRight, 0);
  }

  public void setTitle(@NonNull GlideRequests glideRequests, @Nullable DcContact contact) {
    // the verified state is _not_ shown in the title. this will be confusing as in the one-to-one-ChatViews, the verified
    // icon is also not shown as these chats are always opportunistic chats
    avatar.setAvatar(glideRequests, DcHelper.getContext(getContext()).getRecipient(contact), false);
    title.setText(contact.getDisplayName());
    title.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
    subtitle.setVisibility(View.GONE);
  }

  public void hideAvatar() {
    avatar.setVisibility(View.GONE);
  }

  @Override
  public void setOnClickListener(@Nullable OnClickListener listener) {
    this.content.setOnClickListener(listener);
    this.avatar.setOnClickListener(listener);
  }

  public void setOnAvatarClickListener(@Nullable OnClickListener listener) {
    this.avatar.setOnClickListener(listener);
  }

  @Override
  public void setOnLongClickListener(@Nullable OnLongClickListener listener) {
    this.content.setOnLongClickListener(listener);
    this.avatar.setOnLongClickListener(listener);
  }

  public void setOnBackClickedListener(@Nullable OnClickListener listener) {
    this.back.setOnClickListener(listener);
  }

  private void setComposeTitle() {
    this.title.setText(null);
    this.subtitle.setText(null);
    this.subtitle.setVisibility(View.GONE);
  }

  private void setRecipientTitle(DcChat dcChat, boolean showSubtitle) {

    if(dcChat.getId()==DcChat.DC_CHAT_ID_DEADDROP) {
      this.title.setText(R.string.menu_deaddrop);
      this.subtitle.setText(R.string.menu_deaddrop_subtitle);
    }
    else {
      this.title.setText(dcChat.getName());
      this.subtitle.setText(hackPluralsString(dcChat.getSubtitle()));
    }

    this.subtitle.setVisibility(showSubtitle? View.VISIBLE : View.GONE);
  }

  private String hackPluralsString(String string) {
    // the rust-core does not care about plural forms (there is just once case that was not worth the effort up to now)
    // therefore, we check if the returned string has the form "N member(s)" and localized the corrently to one/few/many/other
    if( string.endsWith(" member(s)") ) {
      int cnt = Util.objectToInt(string.substring(0, string.indexOf(" ")));
      string = getContext().getResources().getQuantityString(R.plurals.n_members, cnt, cnt);
    }
    return string;
  }
}
