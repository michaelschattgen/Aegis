package com.beemdevelopment.aegis.ui.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.beemdevelopment.aegis.AccountNamePosition;
import com.beemdevelopment.aegis.Preferences;
import com.beemdevelopment.aegis.R;
import com.beemdevelopment.aegis.ViewMode;
import com.beemdevelopment.aegis.helpers.AnimationsHelper;
import com.beemdevelopment.aegis.helpers.SimpleAnimationEndListener;
import com.beemdevelopment.aegis.helpers.UiRefresher;
import com.beemdevelopment.aegis.otp.HotpInfo;
import com.beemdevelopment.aegis.otp.OtpInfo;
import com.beemdevelopment.aegis.otp.OtpInfoException;
import com.beemdevelopment.aegis.otp.SteamInfo;
import com.beemdevelopment.aegis.otp.TotpInfo;
import com.beemdevelopment.aegis.otp.YandexInfo;
import com.beemdevelopment.aegis.ui.glide.GlideHelper;
import com.beemdevelopment.aegis.vault.VaultEntry;
import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;

public class EntryHolder extends RecyclerView.ViewHolder {
    private static final float DEFAULT_ALPHA = 1.0f;
    private static final float DIMMED_ALPHA = 0.2f;
    private static final char HIDDEN_CHAR = '●';

    private View _favoriteIndicator;
    private TextView _profileName;
    private TextView _textWindowOffset;
    private RelativeLayout _layoutWindowOffset;
    private TextView _profileCode;
    private TextView _profileIssuer;
    private TextView _profileCopied;
    private ImageView _profileDrawable;
    private VaultEntry _entry;
    private ImageView _buttonRefresh;
    private RelativeLayout _description;
    private ImageView _dragHandle;
    private ViewMode _viewMode;
  
    private final ImageView _selected;
    private final Handler _selectedHandler;

    private Preferences.CodeGrouping _codeGrouping = Preferences.CodeGrouping.NO_GROUPING;
    private AccountNamePosition _accountNamePosition = AccountNamePosition.HIDDEN;

    private boolean _hidden;
    private boolean _paused;
    private int _windowOffset = 0;

    private TotpProgressBar _progressBar;
    private MaterialCardView _view;

    private UiRefresher _refresher;
    private Handler _animationHandler;

    private Animation _scaleIn;
    private Animation _scaleOut;

    public EntryHolder(final View view) {
        super(view);

        _view = (MaterialCardView) view;
        _profileName = view.findViewById(R.id.profile_account_name);
        _profileCode = view.findViewById(R.id.profile_code);
        _profileIssuer = view.findViewById(R.id.profile_issuer);
        _profileCopied = view.findViewById(R.id.profile_copied);
        _textWindowOffset = view.findViewById(R.id.tv_offsetWindow);
        _layoutWindowOffset = view.findViewById(R.id.offsetWindowLayout);
        _description = view.findViewById(R.id.description);
        _profileDrawable = view.findViewById(R.id.ivTextDrawable);
        _buttonRefresh = view.findViewById(R.id.buttonRefresh);
        _selected = view.findViewById(R.id.ivSelected);
        _dragHandle = view.findViewById(R.id.drag_handle);
        _favoriteIndicator = view.findViewById(R.id.favorite_indicator);

        _selectedHandler = new Handler();
        _animationHandler = new Handler();

        _progressBar = view.findViewById(R.id.progressBar);

        _scaleIn = AnimationsHelper.loadScaledAnimation(view.getContext(), R.anim.item_scale_in);
        _scaleOut = AnimationsHelper.loadScaledAnimation(view.getContext(), R.anim.item_scale_out);

        _refresher = new UiRefresher(new UiRefresher.Listener() {
            @Override
            public void onRefresh() {
                if (!_hidden && !_paused) {
                    refreshCode();
                }
            }

            @Override
            public long getMillisTillNextRefresh() {
                return ((TotpInfo) _entry.getInfo()).getMillisTillNextRotation();
            }
        });
    }

    public void setData(VaultEntry entry, Preferences.CodeGrouping groupSize, ViewMode viewMode, AccountNamePosition accountNamePosition, boolean showIcon, boolean showProgress, boolean hidden, boolean paused, boolean dimmed) {
        _entry = entry;
        _hidden = hidden;
        _paused = paused;
        _codeGrouping = groupSize;
        _viewMode = viewMode;
        _accountNamePosition = accountNamePosition;

        _selected.clearAnimation();
        _selected.setVisibility(View.GONE);
        _selectedHandler.removeCallbacksAndMessages(null);
        _animationHandler.removeCallbacksAndMessages(null);

        _favoriteIndicator.setVisibility(_entry.isFavorite() ? View.VISIBLE : View.INVISIBLE);

        // only show the progress bar if there is no uniform period and the entry type is TotpInfo
        setShowProgress(showProgress);

        // only show the button if this entry is of type HotpInfo
        _buttonRefresh.setVisibility(entry.getInfo() instanceof HotpInfo ? View.VISIBLE : View.GONE);

        String profileIssuer = entry.getIssuer();
        String profileName = entry.getName();
        if (!profileIssuer.isEmpty() && !profileName.isEmpty() && _accountNamePosition == AccountNamePosition.END) {
            profileName = _viewMode.getFormattedAccountName(profileName);
        }
        _profileIssuer.setText(profileIssuer);
        _profileName.setText(profileName);
        setAccountNameLayout(_accountNamePosition, !profileIssuer.isEmpty() && !profileName.isEmpty());

        if (_hidden) {
            hideCode();
        } else if (!_paused) {
            refreshCode();
        }

        showIcon(showIcon);

        itemView.setAlpha(dimmed ? DIMMED_ALPHA : DEFAULT_ALPHA);
    }

    private void setAccountNameLayout(AccountNamePosition accountNamePosition, Boolean hasBothIssuerAndName) {
        if (_viewMode == ViewMode.TILES) {
            return;
        }

        RelativeLayout.LayoutParams profileNameLayoutParams;
        RelativeLayout.LayoutParams copiedLayoutParams;
        switch (accountNamePosition) {
            case HIDDEN:
                _profileName.setVisibility(View.GONE);
                break;

            case BELOW:
                profileNameLayoutParams = (RelativeLayout.LayoutParams) _profileName.getLayoutParams();
                profileNameLayoutParams.removeRule(RelativeLayout.END_OF);
                profileNameLayoutParams.addRule(RelativeLayout.BELOW, R.id.profile_issuer);
                profileNameLayoutParams.setMarginStart(0);
                _profileName.setLayoutParams(profileNameLayoutParams);
                _profileName.setVisibility(View.VISIBLE);
                break;

            case END:
            default:
                profileNameLayoutParams = (RelativeLayout.LayoutParams) _profileName.getLayoutParams();
                profileNameLayoutParams.addRule(RelativeLayout.END_OF, R.id.profile_issuer);
                profileNameLayoutParams.removeRule(RelativeLayout.BELOW);
                if (hasBothIssuerAndName) {
                    profileNameLayoutParams.setMarginStart(24);
                }
                _profileName.setLayoutParams(profileNameLayoutParams);
                _profileName.setVisibility(View.VISIBLE);
                break;
        }
    }

    public VaultEntry getEntry() {
        return _entry;
    }

    public void loadIcon(Fragment fragment) {
        GlideHelper.loadEntryIcon(Glide.with(fragment), _entry, _profileDrawable);
    }

    public ImageView getIconView() {
        return _profileDrawable;
    }

    public void setOnRefreshClickListener(View.OnClickListener listener) {
        _buttonRefresh.setOnClickListener(listener);
    }

    public void setShowDragHandle(boolean showDragHandle) {
        if (showDragHandle) {
            _dragHandle.setVisibility(View.VISIBLE);
        } else {
            _dragHandle.setVisibility(View.INVISIBLE);
        }
    }

    public void setShowProgress(boolean showProgress) {
        if (_entry.getInfo() instanceof HotpInfo) {
            showProgress = false;
        }

        _progressBar.setVisibility(showProgress ? View.VISIBLE : View.GONE);
        if (showProgress) {
            _progressBar.setPeriod(((TotpInfo) _entry.getInfo()).getPeriod());
            startRefreshLoop();
        } else {
            stopRefreshLoop();
        }
    }

    public void setFocused(boolean focused) {
        if (focused) {
            _selected.setVisibility(View.VISIBLE);
        }
        _view.setChecked(focused);
    }

    public void setFocusedAndAnimate(boolean focused) {
        setFocused(focused);

        if (focused) {
            _selected.startAnimation(_scaleIn);
        } else {
            _selected.startAnimation(_scaleOut);
            _scaleOut.setAnimationListener(new SimpleAnimationEndListener(animation -> {
                _selected.setVisibility(View.GONE);
            }));
        }
    }

    public void destroy() {
        _refresher.destroy();
    }

    public void startRefreshLoop() {
        _refresher.start();
        _progressBar.start();
    }

    public void stopRefreshLoop() {
        _refresher.stop();
        _progressBar.stop();
    }

    public void refresh() {
        _progressBar.restart();
        refreshCode();
    }

    public void refreshCode() {
        if (!_hidden && !_paused) {
            updateCode();
        }
    }

    public void setWindowOffset(int offset) {
        _windowOffset += offset;
        animateProfileCode(offset);

        updateWindowOffsetView();
    }

    private void updateWindowOffsetView() {
        if (_windowOffset == 0) {
            _layoutWindowOffset.setVisibility(View.INVISIBLE);
        } else {
            _layoutWindowOffset.setVisibility(View.VISIBLE);
            _textWindowOffset.setText((_windowOffset > 0 ? "+" : "") + _windowOffset);
        }
    }

    private void animateProfileCode(int offset) {
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(_profileCode, "alpha", 1f, 0f);
        ObjectAnimator moveRight = ObjectAnimator.ofFloat(_profileCode, "translationX", 0f, offset > 0 ? 40f : -40f);

        AnimatorSet fadeOutSet = new AnimatorSet();
        fadeOutSet.playTogether(fadeOut, moveRight);
        fadeOutSet.setDuration(150);

        fadeOutSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
            updateCode();

            ObjectAnimator fadeIn = ObjectAnimator.ofFloat(_profileCode, "alpha", 0f, 1f);
                _profileCode.setTranslationX(0);
            fadeIn.setDuration(300);
            fadeIn.start();
            }
        });

        fadeOutSet.start();
    }

    private void updateCode() {
        OtpInfo info = _entry.getInfo();

        // In previous versions of Aegis, it was possible to import entries with an empty
        // secret. Attempting to generate OTP's for such entries would result in a crash.
        // In case we encounter an old entry that has this issue, we display "ERROR" as
        // the OTP, instead of crashing.
        String otp;
        try {
            if (_windowOffset != 0 && info instanceof TotpInfo) {
                otp = ((TotpInfo)info).getOtp((System.currentTimeMillis() / 1000) + ((long) _windowOffset * ((TotpInfo) _entry.getInfo()).getPeriod()));
            } else {
                otp = info.getOtp();
            }
            if (!(info instanceof SteamInfo || info instanceof YandexInfo)) {
                otp = formatCode(otp);
            }
        } catch (OtpInfoException e) {
            otp = _view.getResources().getString(R.string.error_all_caps);
        }

        _profileCode.setText(otp);
    }

    private String formatCode(String code) {
        int groupSize;
        StringBuilder sb = new StringBuilder();

        switch (_codeGrouping) {
            case NO_GROUPING:
                groupSize = code.length();
                break;
            case HALVES:
                groupSize = (code.length() / 2) + (code.length() % 2);
                break;
            default:
                groupSize = _codeGrouping.getValue();
                if (groupSize <= 0) {
                    throw new IllegalArgumentException("Code group size cannot be zero or negative");
                }
        }

        for (int i = 0; i < code.length(); i++) {
            if (i != 0 && i % groupSize == 0) {
                sb.append(" ");
            }
            sb.append(code.charAt(i));
        }
        code = sb.toString();

        return code;
    }

    public void revealCode() {
        updateCode();
        _hidden = false;
    }

    public void hideCode() {
        String hiddenText = new String(new char[_entry.getInfo().getDigits()]).replace("\0", Character.toString(HIDDEN_CHAR));
        hiddenText = formatCode(hiddenText);
        _profileCode.setText(hiddenText);
        _hidden = true;
    }

    public void showIcon(boolean show) {
        if (show) {
            _profileDrawable.setVisibility(View.VISIBLE);
        } else {
            _profileDrawable.setVisibility(View.GONE);
        }
    }

    public boolean isHidden() {
        return _hidden;
    }

    public void setPaused(boolean paused) {
        _paused = paused;

        if (!_hidden && !_paused) {
            updateCode();
        }
    }

    public void dim() {
        animateAlphaTo(DIMMED_ALPHA);
    }

    public void highlight() {
        animateAlphaTo(DEFAULT_ALPHA);
    }

    public void animateCopyText(boolean includeSlideAnimation) {
        _animationHandler.removeCallbacksAndMessages(null);

        Animation slideDownFadeIn = AnimationsHelper.loadScaledAnimation(itemView.getContext(), R.anim.slide_down_fade_in);
        Animation slideDownFadeOut = AnimationsHelper.loadScaledAnimation(itemView.getContext(), R.anim.slide_down_fade_out);
        Animation fadeOut = AnimationsHelper.loadScaledAnimation(itemView.getContext(), R.anim.fade_out);
        Animation fadeIn = AnimationsHelper.loadScaledAnimation(itemView.getContext(), R.anim.fade_in);

        if (includeSlideAnimation) {
            _profileCopied.startAnimation(slideDownFadeIn);
           View fadeOutView = (_accountNamePosition == AccountNamePosition.BELOW) ? _profileName : _description;

        fadeOutView.startAnimation(slideDownFadeOut);

            _animationHandler.postDelayed(() -> {
                _profileCopied.startAnimation(fadeOut);
                fadeOutView.startAnimation(fadeIn);
            }, 3000);
        } else {
            _profileCopied.startAnimation(fadeIn);
            _profileName.startAnimation(fadeOut);

            _animationHandler.postDelayed(() -> {
                _profileCopied.startAnimation(fadeOut);
                _profileName.startAnimation(fadeIn);
            }, 3000);
        }
    }

    private void animateAlphaTo(float alpha) {
        itemView.animate().alpha(alpha).setDuration(200).start();
    }
}
