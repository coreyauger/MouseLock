

import java.applet.Applet;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;
import java.awt.Toolkit;
import java.util.Timer;
import java.util.TimerTask;
import netscape.javascript.JSObject;

public class MouseLockApplet extends Applet{

	private String msg = "empty";
	private Robot _robot = null;
//	private Dimension _screenDims = null;
	private Timer _timer = null;
	private boolean _enable = true;
	private JSObject _window = null;
	
	private Point _delta = new Point();
	private Point _last = new Point();
	private Point _center = null;
	private int _sampleRate = 50;	
	private int _safty = 0;
	
	private boolean _enableMouseCapture = false;
	
	public MouseLockApplet(){
		try {	
			_robot = new Robot();
			//_screenDims = Toolkit.getDefaultToolkit().getScreenSize();			
			//_center = new Point( _screenDims.width/2, _screenDims.height/2);			// assuming 4/3 aspect ratio
			//_last = new Point(_center.x, _center.y);
			setSampleRate(_sampleRate);
		//	_center = MouseInfo.getPointerInfo().getLocation();
		} catch (Exception e) {
			e.printStackTrace();
			msg = e.getMessage();
			msg = e.getMessage();
			String param1 = "MouseLockApplet: " + msg ;
		    Object[] params = {param1};
			_window.call("debug", params);
		}
	}		 
	
	
	private void captureMouseCenter(){
		try{		
			_center = MouseInfo.getPointerInfo().getLocation();
			_last = new Point(_center.x, _center.y);
		}catch (Exception e) {
			e.printStackTrace();
			msg = e.getMessage();
			String param1 = "assignMouseToCenter: " + msg ;
		    Object[] params = {param1};
			_window.call("debug", params);
		}	
	}
	
	public void assignMouseToCenter(){		
		// (CA) - this is called from javascript .. which will cause a security exception
		// to do anything (like capture the mouse cursor) ... so we need to trick it and
		// signal something in the main run loop to do the capture..
		_enableMouseCapture = true;
	}
	
	public void setNewCenter(int top, int left, int width, int height){
		_center = new Point( (int)(left+(width*0.5)), (int)(top+(height*0.5)));
	}
	
	public void start() {
		try{				
			_window = JSObject.getWindow(this);	
			_window.call("appletStart", null);		
		}catch (Exception e) {
			e.printStackTrace();
			msg = e.getMessage();
		}
	}
	
	public void setEnable(boolean enable){		
		_safty = 0;
		_enable = enable;
	}

	public void setSampleRate(int millis){
		if( millis < 20 ){
			millis = 20; 	// no sampling faster then 50 times a second.
		}
		_sampleRate = millis;
		final int safeCount = 300000/_sampleRate ;
		try{
			if( _timer != null ){
				_timer.cancel();				
			}
			_timer = new Timer();
			_timer.schedule(new TimerTask() {				
				@Override
				public void run() {
					if( _enableMouseCapture ){
						captureMouseCenter();
						_enableMouseCapture = false;
					}
					if( _enable && _center != null){
						//msg = "";
						if( _safty < safeCount ){
							Point p =  MouseInfo.getPointerInfo().getLocation();
							_delta.x = (p.x - _center.x);
							_delta.y = (p.y - _center.y);
							if( _delta.x == 0 && _delta.y == 0 ){
								_safty++;								
							}else{
								_safty = 0;
							}
							_robot.mouseMove(_center.x, _center.y);
							if( _last.x != _delta.x || _last.y != _delta.y){
								try {
									    Integer param1 = _delta.x ;
									    Integer param2 = _delta.y;
									    Object[] params = {param1, param2};
									    // (CA) - this will now call your Javascript updatePosition function in the client browser.
									    _window.call("updatePosition", params);
								  } catch (Exception e) {
									  e.printStackTrace();
								  }								
							}
							_last.x = _delta.x;
							_last.y = _delta.y;
						}
						// CA - leave in for debug...
						//repaint();
					}
				}
			}, 2000, _sampleRate);
		} catch (Exception e) {
			e.printStackTrace();
			msg = e.getMessage();
		}
	}
	

	// hack debug
	public void paint(Graphics g) {

		try{
			g.setColor(Color.blue);
	        g.drawString("test: " + msg, 20, 20);
		} catch (Exception e) {
			g.drawString("e: " + e.getMessage(), 20, 20);		
		}
        
              
    }	
	
}

