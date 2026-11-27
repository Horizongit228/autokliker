package com.autoclickx.app;
import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.*;
import android.os.*;
import android.view.*;
import android.view.accessibility.AccessibilityEvent;
import android.widget.*;
public class ClickService extends AccessibilityService {
 static ClickService instance;
 private final Handler handler=new Handler(Looper.getMainLooper());
 private WindowManager wm; private View marker; private LinearLayout panel; private WindowManager.LayoutParams markerParams; private Button start,stop;
 private boolean running; private int epoch,interval; private float targetX,targetY; private long lastStart;
 private int dp(float v){return Math.round(v*getResources().getDisplayMetrics().density);}
 @Override protected void onServiceConnected(){super.onServiceConnected();instance=this;wm=(WindowManager)getSystemService(WINDOW_SERVICE);showOverlay();}
 private WindowManager.LayoutParams params(int w,int h,int gravity){WindowManager.LayoutParams p=new WindowManager.LayoutParams(w,h,WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,PixelFormat.TRANSLUCENT);p.gravity=gravity;return p;}
 public void showOverlay(){
  if(wm==null||panel!=null)return;
  marker=new View(this){private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);@Override protected void onDraw(Canvas c){float x=getWidth()/2f,y=getHeight()/2f,r=Math.min(x,y)-dp(3);paint.setStyle(Paint.Style.FILL);paint.setColor(0x5530DCA0);c.drawCircle(x,y,r,paint);paint.setColor(0xff65e2b1);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(dp(2));c.drawCircle(x,y,r,paint);c.drawLine(x-dp(10),y,x+dp(10),y,paint);c.drawLine(x,y-dp(10),x,y+dp(10),paint);}};
  marker.setContentDescription("Перетащите точку нажатия");markerParams=params(dp(56),dp(56),Gravity.TOP|Gravity.LEFT);android.content.SharedPreferences prefs=getSharedPreferences("settings",MODE_PRIVATE);markerParams.x=Math.max(0,Math.min(getResources().getDisplayMetrics().widthPixels-dp(56),prefs.getInt("marker_x",getResources().getDisplayMetrics().widthPixels/2-dp(28))));markerParams.y=Math.max(0,Math.min(getResources().getDisplayMetrics().heightPixels-dp(56),prefs.getInt("marker_y",dp(160))));
  marker.setOnTouchListener(new View.OnTouchListener(){float sx,sy;int ox,oy;@Override public boolean onTouch(View v,MotionEvent e){if(e.getActionMasked()==MotionEvent.ACTION_DOWN){sx=e.getRawX();sy=e.getRawY();ox=markerParams.x;oy=markerParams.y;return true;}if(e.getActionMasked()==MotionEvent.ACTION_MOVE){markerParams.x=ox+Math.round(e.getRawX()-sx);markerParams.y=oy+Math.round(e.getRawY()-sy);wm.updateViewLayout(marker,markerParams);}if(e.getActionMasked()==MotionEvent.ACTION_UP){getSharedPreferences("settings",MODE_PRIVATE).edit().putInt("marker_x",markerParams.x).putInt("marker_y",markerParams.y).apply();}return true;}});
  panel=new LinearLayout(this);panel.setOrientation(LinearLayout.HORIZONTAL);panel.setPadding(dp(6),dp(6),dp(6),dp(6));panel.setBackground(round(0xf21b2333,20));panel.setElevation(dp(8));
  start=button("▶",0xffa7f3d0,0xff10251d);start.setContentDescription("Старт");stop=button("■",0xfffb7185,0xff36131a);stop.setContentDescription("Стоп");Button hide=button("×",0xff344155,0xfff1f5f9);hide.setContentDescription("Скрыть панель");stop.setEnabled(false);stop.setAlpha(.45f);start.setOnClickListener(v->startClicks());stop.setOnClickListener(v->stopClicks());hide.setOnClickListener(v->hideOverlay());panel.addView(start);panel.addView(stop);panel.addView(hide);
  try{wm.addView(marker,markerParams);WindowManager.LayoutParams pp=params(-2,-2,Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL);pp.y=dp(24);wm.addView(panel,pp);}catch(RuntimeException e){hideOverlay();Toast.makeText(this,"Не удалось показать панель",Toast.LENGTH_SHORT).show();}
 }
 private android.graphics.drawable.GradientDrawable round(int color,int radius){android.graphics.drawable.GradientDrawable d=new android.graphics.drawable.GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));return d;}
 private Button button(String text,int bg,int fg){Button b=new Button(this);b.setText(text);b.setTextSize(20);b.setTextColor(fg);b.setAllCaps(false);b.setPadding(0,0,0,0);b.setBackground(new android.graphics.drawable.RippleDrawable(android.content.res.ColorStateList.valueOf(0x33ffffff),round(bg,15),null));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(dp(54),dp(50));lp.setMargins(dp(3),0,dp(3),0);b.setLayoutParams(lp);return b;}
 private void startClicks(){
  if(running||marker==null)return;int[] loc=new int[2];marker.getLocationOnScreen(loc);targetX=loc[0]+marker.getWidth()/2f;targetY=loc[1]+marker.getHeight()/2f;int[] p=new int[2];panel.getLocationOnScreen(p);
  if(targetX>=p[0]&&targetX<p[0]+panel.getWidth()&&targetY>=p[1]&&targetY<p[1]+panel.getHeight()){Toast.makeText(this,"Переместите мишень за пределы панели",Toast.LENGTH_SHORT).show();return;}
  interval=Math.max(1,Math.min(60000,getSharedPreferences("settings",MODE_PRIVATE).getInt("interval",500)));running=true;int token=++epoch;
  marker.setVisibility(View.VISIBLE);marker.setAlpha(.82f);markerParams.flags|=WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;wm.updateViewLayout(marker,markerParams);start.setEnabled(false);start.setAlpha(.45f);stop.setEnabled(true);stop.setAlpha(1f);handler.postDelayed(()->tap(token),150);
 }
 private void tap(int token){
  if(!running||token!=epoch)return;Path path=new Path();path.moveTo(targetX,targetY);GestureDescription g=new GestureDescription.Builder().addStroke(new GestureDescription.StrokeDescription(path,0,Math.max(1,Math.min(20,interval)))).build();lastStart=SystemClock.uptimeMillis();
  try{boolean accepted=dispatchGesture(g,new GestureResultCallback(){@Override public void onCompleted(GestureDescription gesture){schedule(token);}@Override public void onCancelled(GestureDescription gesture){if(running&&token==epoch)stopClicks();}},handler);if(!accepted)stopClicks();}catch(RuntimeException e){stopClicks();}
 }
 private void schedule(int token){if(running&&token==epoch)handler.postDelayed(()->tap(token),Math.max(1,interval-(SystemClock.uptimeMillis()-lastStart)));}
 private void stopClicks(){running=false;epoch++;handler.removeCallbacksAndMessages(null);if(marker!=null){marker.setVisibility(View.VISIBLE);marker.setAlpha(1f);markerParams.flags&=~WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;if(marker.isAttachedToWindow())wm.updateViewLayout(marker,markerParams);}if(start!=null){start.setEnabled(true);start.setAlpha(1f);}if(stop!=null){stop.setEnabled(false);stop.setAlpha(.45f);}}
 private void hideOverlay(){stopClicks();if(wm!=null){if(marker!=null&&marker.isAttachedToWindow())wm.removeViewImmediate(marker);if(panel!=null&&panel.isAttachedToWindow())wm.removeViewImmediate(panel);}marker=null;panel=null;start=null;stop=null;}
 @Override public void onAccessibilityEvent(AccessibilityEvent event){}
 @Override public void onInterrupt(){stopClicks();}
 @Override public boolean onUnbind(android.content.Intent intent){hideOverlay();if(instance==this)instance=null;return super.onUnbind(intent);}
 @Override public void onDestroy(){hideOverlay();if(instance==this)instance=null;super.onDestroy();}
}
