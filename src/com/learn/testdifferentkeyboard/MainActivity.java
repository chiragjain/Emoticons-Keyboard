package com.learn.testdifferentkeyboard;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.StringTokenizer;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.text.Html.ImageGetter;
import android.text.Spanned;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;

import com.learn.testdifferentkeyboard.EmoticonsGridAdapter.KeyClickListener;

/**
 * Main activity for chat window
 * 
 * @author Chirag Jain
 */
public class MainActivity extends FragmentActivity implements KeyClickListener {
	
	private static final int NO_OF_EMOTICONS = 54;

	private ListView chatList;
	private View popUpView;
	private ArrayList<Spanned> chats;
	private ChatListAdapter mAdapter;

	private LinearLayout emoticonsCover;
	private PopupWindow popupWindow;

	private int keyboardHeight;	
	private EditText content;
	
	private LinearLayout parentLayout;

	private boolean isKeyBoardVisible;
	
	private Bitmap[] emoticons;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		chatList = (ListView) findViewById(R.id.chat_list);		

		parentLayout = (LinearLayout) findViewById(R.id.list_parent);

		emoticonsCover = (LinearLayout) findViewById(R.id.footer_for_emoticons);

		popUpView = getLayoutInflater().inflate(R.layout.emoticons_popup, null);

		// Setting adapter for chat list
		chats = new ArrayList<Spanned>();
		mAdapter = new ChatListAdapter(getApplicationContext(), chats);
		chatList.setAdapter(mAdapter);
		chatList.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (popupWindow.isShowing())
					popupWindow.dismiss();	
				return false;
			}
		});

		// Defining default height of keyboard which is equal to 230 dip
		final float popUpheight = getResources().getDimension(
				R.dimen.keyboard_height);
		changeKeyboardHeight((int) popUpheight);
		
		// Showing and Dismissing pop up on clicking emoticons button
		ImageView emoticonsButton = (ImageView) findViewById(R.id.emoticons_button);
		emoticonsButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (!popupWindow.isShowing()) {

					popupWindow.setHeight((int) (keyboardHeight));

					if (isKeyBoardVisible) {
						emoticonsCover.setVisibility(LinearLayout.GONE);
					} else {
						emoticonsCover.setVisibility(LinearLayout.VISIBLE);
					}
					popupWindow.showAtLocation(parentLayout, Gravity.BOTTOM, 0, 0);

				} else {
					popupWindow.dismiss();
				}

			}
		});
		
		readEmoticons();
		enablePopUpView();
		checkKeyboardHeight(parentLayout);
		enableFooterView();

	}
	
	/**
	 * Reading all emoticons in local cache
	 */
	private void readEmoticons () {
		
		emoticons = new Bitmap[NO_OF_EMOTICONS];
		for (short i = 0; i < NO_OF_EMOTICONS; i++) {			
			emoticons[i] = getImage((i+1) + ".png");
		}
		
	}

	/**
	 * Enabling all content in footer i.e. post window
	 */
	private void enableFooterView() {

		content = (EditText) findViewById(R.id.chat_content);
		content.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				if (popupWindow.isShowing()) {
					
					popupWindow.dismiss();
					
				}
				
			}
		});
		final Button postButton = (Button) findViewById(R.id.post_button);		
		
		postButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (content.getText().toString().length() > 0) {
					
					Spanned sp = content.getText();					
					chats.add(sp);
					content.setText("");					
					mAdapter.notifyDataSetChanged();

				}

			}
		});
	}

	/**
	 * Overriding onKeyDown for dismissing keyboard on key down
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (popupWindow.isShowing()) {
			popupWindow.dismiss();
			return false;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}
	
	/**
	 * Checking keyboard height and keyboard visibility
	 */
	int previousHeightDiffrence = 0;
	private void checkKeyboardHeight(final View parentLayout) {

		parentLayout.getViewTreeObserver().addOnGlobalLayoutListener(
				new ViewTreeObserver.OnGlobalLayoutListener() {

					@Override
					public void onGlobalLayout() {
						
						Rect r = new Rect();
						parentLayout.getWindowVisibleDisplayFrame(r);
						
						int screenHeight = parentLayout.getRootView()
								.getHeight();
						int heightDifference = screenHeight - (r.bottom);
						
						if (previousHeightDiffrence - heightDifference > 50) {							
							popupWindow.dismiss();
						}
						
						previousHeightDiffrence = heightDifference;
						if (heightDifference > 100) {

							isKeyBoardVisible = true;
							changeKeyboardHeight(heightDifference);

						} else {

							isKeyBoardVisible = false;
							
						}

					}
				});

	}

	/**
	 * change height of emoticons keyboard according to height of actual
	 * keyboard
	 * 
	 * @param height
	 *            minimum height by which we can make sure actual keyboard is
	 *            open or not
	 */
	private void changeKeyboardHeight(int height) {

		if (height > 100) {
			keyboardHeight = height;
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, keyboardHeight);
			emoticonsCover.setLayoutParams(params);
		}

	}

	/**
	 * Defining all components of emoticons keyboard
	 */
	private void enablePopUpView() {

		ViewPager pager = (ViewPager) popUpView.findViewById(R.id.emoticons_pager);
		pager.setOffscreenPageLimit(3);
		
		ArrayList<String> paths = new ArrayList<String>();

		for (short i = 1; i <= NO_OF_EMOTICONS; i++) {			
			paths.add(i + ".png");
		}

		EmoticonsPagerAdapter adapter = new EmoticonsPagerAdapter(MainActivity.this, paths, this);
		pager.setAdapter(adapter);

		// Creating a pop window for emoticons keyboard
		popupWindow = new PopupWindow(popUpView, LayoutParams.MATCH_PARENT,
				(int) keyboardHeight, false);
		
		TextView backSpace = (TextView) popUpView.findViewById(R.id.back);
		backSpace.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				KeyEvent event = new KeyEvent(0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL);
				content.dispatchKeyEvent(event);	
			}
		});

		popupWindow.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss() {
				emoticonsCover.setVisibility(LinearLayout.GONE);
			}
		});
	}

	/**
	 * For loading smileys from assets
	 */
	private Bitmap getImage(String path) {
		AssetManager mngr = getAssets();
		InputStream in = null;
		try {
			in = mngr.open("emoticons/" + path);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		Bitmap temp = BitmapFactory.decodeStream(in, null, null);
		return temp;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public void keyClickedIndex(final String index) {
		
		ImageGetter imageGetter = new ImageGetter() {
            public Drawable getDrawable(String source) {    
            	StringTokenizer st = new StringTokenizer(index, ".");
                Drawable d = new BitmapDrawable(getResources(),emoticons[Integer.parseInt(st.nextToken()) - 1]);
                d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
                return d;
            }
        };
        
        Spanned cs = Html.fromHtml("<img src ='"+ index +"'/>", imageGetter, null);        
		
		int cursorPosition = content.getSelectionStart();		
        content.getText().insert(cursorPosition, cs);
        
	}

}
