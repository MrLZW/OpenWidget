package org.mrlzw.opencomponent;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Scroller;
@SuppressWarnings("deprecation")
public class SlideListView extends ListView{
	private final static int SPEED=5;
	private int MIN_MOVE_DISTANCE=5;
	private int SCREEN_WIDTH;
	private VelocityTracker tracker;
	private Scroller scroller;
	private SlideListener slideListener;
	private View goalItemView;


	private int goalItemID;

	private int downX;
	private int downY;

	private boolean isSlide=false;

	public SlideListView(Context context) {
		super(context);
		SCREEN_WIDTH = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getWidth();

		scroller = new Scroller(context);
	}
	public SlideListView(Context context,int lLayout,int rLayout) {
		super(context);
		SCREEN_WIDTH = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getWidth();
		MIN_MOVE_DISTANCE = ViewConfiguration.get(getContext()).getScaledTouchSlop();
		scroller = new Scroller(context);
	}
	public SlideListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		SCREEN_WIDTH = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getWidth();
		scroller = new Scroller(context);
	}
	public SlideListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		SCREEN_WIDTH = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getWidth();
		scroller = new Scroller(context);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (isSlide && goalItemID != AdapterView.INVALID_POSITION) {
			addVelocityTracker(ev);
			final int action = ev.getAction();
			int x = (int) ev.getX();
			switch (action) {
			case MotionEvent.ACTION_MOVE:
				int deltaX = downX - x;
				downX = x;
				goalItemView.scrollBy(deltaX, 0);
				break;
			case MotionEvent.ACTION_UP:
				int velocityX = getScrollVelocity();
				if (velocityX > SPEED) {
					scrollRight();
				} else if (velocityX < -SPEED) {
					scrollLeft();
				} else {
					scrollByDistanceX();
				}
				removeVelocityTracker();
				isSlide = false;
				break;
			}
			return true; 
		}
		return super.onTouchEvent(ev);
	}


	private void scrollRight() {

		final int delta = (SCREEN_WIDTH + goalItemView.getScrollX());

		scroller.startScroll(goalItemView.getScrollX(), 0, -delta, 0,
				Math.abs(delta));
		postInvalidate(); 
	}


	private void scrollLeft() {

		final int delta = (SCREEN_WIDTH - goalItemView.getScrollX());

		scroller.startScroll(goalItemView.getScrollX(), 0, delta, 0,
				Math.abs(delta));
		postInvalidate(); 
	}


	private void scrollByDistanceX() {

		if (goalItemView.getScrollX() >= SCREEN_WIDTH /10) {
			scrollLeft();
		} else if (goalItemView.getScrollX() <= -SCREEN_WIDTH /10) {
			scrollRight();
		} else {
			goalItemView.scrollTo(0, 0);
		}

	}
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev){
		switch(ev.getAction()){
		case MotionEvent.ACTION_DOWN: {
			addVelocityTracker(ev);
			if (!scroller.isFinished()) {
				return super.dispatchTouchEvent(ev);
			}
			downX = (int) ev.getX();
			downY = (int) ev.getY();
			goalItemID = pointToPosition(downX, downY);
			if (goalItemID == AdapterView.INVALID_POSITION) {
				return super.dispatchTouchEvent(ev);
			}
			goalItemView=getChildAt(goalItemID);
			break;
		}
		case MotionEvent.ACTION_MOVE: {
			if (Math.abs(getScrollVelocity()) > SPEED
					|| (Math.abs(ev.getX() - downX) > MIN_MOVE_DISTANCE && Math
							.abs(ev.getY() - downY) < MIN_MOVE_DISTANCE)) {
				isSlide = true;
			}
			break;
		}
		case MotionEvent.ACTION_UP:
			removeVelocityTracker();
			break;
		}
		return super.dispatchTouchEvent(ev);
	}
	@Override
	public void computeScroll() {

		if (scroller.computeScrollOffset()) {

			goalItemView.scrollTo(scroller.getCurrX(), scroller.getCurrY());
			postInvalidate();

			if (scroller.isFinished()) {
				if (slideListener == null) {
				
				}
				goalItemView.scrollTo(0, 0);
				slideListener.afterSlide(goalItemView);
			}
		}
	}
	public void addVelocityTracker(MotionEvent ev){
		if (tracker == null) {
			tracker = VelocityTracker.obtain();
		}

		tracker.addMovement(ev);
	}
	public void removeVelocityTracker(){
		if (tracker != null) {
			tracker.recycle();
			tracker = null;
		}
	}
	public void setSlideListener(SlideListener listener){
		slideListener=listener;
	}
	public int getScrollVelocity(){
		tracker.computeCurrentVelocity(1000);
		int velocity = (int) tracker.getXVelocity();
		return velocity;
	}
	public interface SlideListener{
		public void afterSlide(View v);
	};
}
