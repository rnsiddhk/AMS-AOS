package com.cashnet.common;

import java.util.ArrayList;

import com.cashnet.R;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * Popup window, shows action list as icon and text like the one in Gallery3D app.
 *
 * @author Lorensius. W. T
 */
public class QuickActionVertical extends CustomPopupWindow {
    private final View root, view;

    private final ImageView mArrowUp;

    private final ImageView mArrowDown;

    private final LayoutInflater inflater;

    private final Context context;

    private int popupWidth = 0;

    protected static final int ANIM_GROW_FROM_LEFT = 1;

    protected static final int ANIM_GROW_FROM_RIGHT = 2;

    protected static final int ANIM_GROW_FROM_CENTER = 3;

    protected static final int ANIM_REFLECT = 4;


    protected static final int ANIM_AUTO = 5;

    private int animStyle;

    private ViewGroup mTrack;

    private ScrollView scroller;

    private ArrayList<ActionItemVertical> actionList;

    /**
     * Constructor
     *
     * @param anchor {@link View} on where the popup window should be displayed
     */
    public QuickActionVertical(View anchor) {
        super( anchor);

        actionList = new ArrayList<ActionItemVertical>();
        context = anchor.getContext();
        inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE);

        root = (ViewGroup) inflater.inflate( R.layout.popup, null);

        mArrowDown = (ImageView) root.findViewById( R.id.arrow_down);
        mArrowUp = (ImageView) root.findViewById( R.id.arrow_up);

        setContentView(root);

        mTrack = (ViewGroup) root.findViewById( R.id.tracks);
        scroller = (ScrollView) root.findViewById( R.id.scroller);
        animStyle = ANIM_AUTO;
        this.view = anchor;
    }



    public QuickActionVertical(View anchor, int parentWidth) {
        super( anchor);

        actionList = new ArrayList<ActionItemVertical>();
        context = anchor.getContext();
        inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE);

        root = (ViewGroup) inflater.inflate( R.layout.popup, null);

        mArrowDown = (ImageView) root.findViewById( R.id.arrow_down);
        mArrowUp = (ImageView) root.findViewById( R.id.arrow_up);

        setContentView( root);

        mTrack = (ViewGroup) root.findViewById( R.id.tracks);
        scroller = (ScrollView) root.findViewById( R.id.scroller);
        animStyle = ANIM_AUTO;
        this.view = anchor;
        popupWidth = parentWidth;
    }
    /**
     * Set animation style
     *
     * @param animStyle animation style, default is set to ANIM_AUTO
     */
    public void setAnimStyle(int animStyle) {
        this.animStyle = animStyle;
    }

    /**
     * Add action item
     *
     */
    public void addActionItem(ActionItemVertical action) {
        actionList.add( action);
    }

    /**
     * Show popup window. Popup is automatically positioned, on top or bottom of anchor view.
     *
     */
    public void show() {
        preShow();

        int xPos, yPos;

        int[] location = new int[2];

        // 클릭된 View의 X, Y좌표를 계산해서 return 한다. (부모 View 기준이 아닌 screenSize기준 좌표를 가져온다.)
        anchor.getLocationOnScreen( location);

        // left, top, right, bottom (anchor는 클릭된 View)
        Rect anchorRect = new Rect( location[0], location[1], location[0] + anchor.getWidth(), location[1]
                + anchor.getHeight());

        createActionList();

        // root는 생성된 데이터를 뿌려주는 화면의 길이(퀵액션의 전체 길이). 리스트가 많을수록 Height는 길어진다.
        root.setLayoutParams( new LayoutParams( LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        root.measure( LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

        int rootHeight = root.getMeasuredHeight();
        int rootWidth = root.getMeasuredWidth();

        // 스크린 사이즈, 1280 * 800이 정상이나 하단 bar 때문에 1280*752
        int screenWidth = windowManager.getDefaultDisplay().getWidth();
        int screenHeight = windowManager.getDefaultDisplay().getHeight();

        // 팝업의 xPos를 구할 때는 다른 방식으로 구해야함. How?
        // 팝업의 x좌표를 얻어온다.
        // 클릭된 View의 left 좌표 + 팝업창의 Width > screenWidth보다 크면

        // 팝업 View의 Width값을 넘겨받았으면
        if(popupWidth == 0){
            if((anchorRect.left + rootWidth) > screenWidth) {
                xPos = anchorRect.left - (rootWidth - anchor.getWidth());
            }
            else {
                // 클릭된 View의 Width가 팝업창의 Width보다 크면
                if(anchor.getWidth() > rootWidth) {
                    xPos = anchorRect.centerX() - (rootWidth / 2);
                }
                else {
                    xPos = anchorRect.left;
                }
            }
            Log.i("popupWidth is zero", xPos + "");

        }else{

            xPos = 440;
            Log.i("popupWidth is not zero", xPos + "");
        }


        int dyTop = anchorRect.top;
        int dyBottom = screenHeight - anchorRect.bottom;	// 752 - anchorRect.bottom

        boolean onTop = (dyTop > dyBottom) ? true : false;

        if(onTop) {
            if(rootHeight > dyTop) {
                yPos = 15;
                LayoutParams l = scroller.getLayoutParams();
                l.height = dyTop - anchor.getHeight();
            }
            else {
                yPos = anchorRect.top - rootHeight;
            }
        }
        else {
            yPos = anchorRect.bottom;

            if(rootHeight > dyBottom) {
                LayoutParams l = scroller.getLayoutParams();
                l.height = dyBottom;
            }
        }

        showArrow( ((onTop) ? R.id.arrow_down : R.id.arrow_up), anchorRect.centerX() - xPos);

        setAnimationStyle( screenWidth, anchorRect.centerX(), onTop);

        switch (view.getId()) {


            default:
                // Gravity : 팝업의 정렬 위치 지정, parent의 중앙, 오른쪽, 아래쪽 등등
                // 팝업창이 뜰 때는 부모의 위치부터 xPos를 계산하는 것이 아니라 팝업의 위치부터 xPos를 계산해서 팝업을 뿌려준다. (NO_GRAVITY)
                // xPos, yPos는 팝업치 출력될 좌표이되 절대 좌표가 아니며 Gravity 적용 후 거기서부터 얼마나 이동할 지 결정.
                window.showAtLocation(anchor, Gravity.NO_GRAVITY, xPos, yPos);
                break;
        }

    }

    /**
     * Set animation style
     *
     * @param screenWidth screen width	// 스크린의 Width
     * @param requestedX distance from left edge	// 좌측 꼭짓점부터의 거리
     * @param onTop flag to indicate where the popup should be displayed. Set TRUE if displayed on top of anchor view
     * and vice versa		// 팝업이 클릭된 view(anchor)의 top에 있으면 true, 아니면 false
     */
    private void setAnimationStyle(int screenWidth, int requestedX, boolean onTop) {
        int arrowPos = requestedX - mArrowUp.getMeasuredWidth() / 2;

        switch (animStyle) {
            case ANIM_GROW_FROM_LEFT:
                window.setAnimationStyle( (onTop) ? R.style.Animations_PopUpMenu_Left
                        : R.style.Animations_PopDownMenu_Left);
                break;

            case ANIM_GROW_FROM_RIGHT:
                window.setAnimationStyle( (onTop) ? R.style.Animations_PopUpMenu_Right
                        : R.style.Animations_PopDownMenu_Right);
                break;

            case ANIM_GROW_FROM_CENTER:
                window.setAnimationStyle( (onTop) ? R.style.Animations_PopUpMenu_Center
                        : R.style.Animations_PopDownMenu_Center);
                break;

            case ANIM_REFLECT:
                window.setAnimationStyle( (onTop) ? R.style.Animations_PopUpMenu_Reflect
                        : R.style.Animations_PopDownMenu_Reflect);
                break;

            case ANIM_AUTO:
                if(arrowPos <= screenWidth / 4) {
                    window.setAnimationStyle( (onTop) ? R.style.Animations_PopUpMenu_Left
                            : R.style.Animations_PopDownMenu_Left);
                }
                else if(arrowPos > screenWidth / 4 && arrowPos < 3 * (screenWidth / 4)) {
                    window.setAnimationStyle( (onTop) ? R.style.Animations_PopUpMenu_Center
                            : R.style.Animations_PopDownMenu_Center);
                }
                else {
                    window.setAnimationStyle( (onTop) ? R.style.Animations_PopUpMenu_Right
                            : R.style.Animations_PopDownMenu_Right);
                }

                break;
        }
    }

    /**
     * Create action list
     */
    private void createActionList() {
        View view;
        String title;
        Drawable icon;
        OnClickListener listener;

        for( int i = 0; i < actionList.size(); i++) {
            title = actionList.get( i).getTitle();
            icon = actionList.get( i).getIcon();
            listener = actionList.get( i).getListener();

            view = getActionItem( title, icon, listener);

            view.setFocusable( true);
            view.setClickable( true);

            mTrack.addView( view);
        }
    }

    /**
     * Get action item {@link View}
     *
     * @param title action item title
     * @param icon {@link Drawable} action item icon
     * @param listener {@link View.OnClickListener} action item listener
     * @return action item {@link View}
     */
    private View getActionItem(String title, Drawable icon, OnClickListener listener) {
        LinearLayout container = (LinearLayout) inflater.inflate( R.layout.action_item, null);

        ImageView img = (ImageView) container.findViewById( R.id.icon);
        TextView text = (TextView) container.findViewById( R.id.title);
//        text.setTypeface( LoginView.face);

        if(icon != null) {
            img.setImageDrawable( icon);
        }

        if(title != null) {
            text.setText( title);
        }

        if(listener != null) {
            container.setOnClickListener( listener);
        }

        return container;
    }

    /**
     * Show arrow
     *
     * @param whichArrow arrow type resource id
     * @param requestedX distance from left screen
     */
    private void showArrow(int whichArrow, int requestedX) {
        final View showArrow = (whichArrow == R.id.arrow_up) ? mArrowUp : mArrowDown;
        final View hideArrow = (whichArrow == R.id.arrow_up) ? mArrowDown : mArrowUp;

        final int arrowWidth = mArrowUp.getMeasuredWidth();

        showArrow.setVisibility( View.VISIBLE);

        ViewGroup.MarginLayoutParams param = (ViewGroup.MarginLayoutParams) showArrow.getLayoutParams();

        param.leftMargin = requestedX - arrowWidth / 2;

        hideArrow.setVisibility( View.INVISIBLE);
    }
}