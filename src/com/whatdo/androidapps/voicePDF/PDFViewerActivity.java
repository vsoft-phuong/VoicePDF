package com.whatdo.androidapps.voicePDF;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

import net.sf.andpdf.nio.ByteBuffer;
import net.sf.andpdf.refs.HardReference;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFImage;
import com.sun.pdfview.PDFPage;
import com.sun.pdfview.PDFPaint;
import com.sun.pdfview.decrypt.PDFAuthenticationFailureException;
import com.sun.pdfview.decrypt.PDFPassword;
import com.sun.pdfview.font.PDFFont;

import com.whatdo.androidapps.voicePDF.views.PDFImageView;
import com.whatdo.androidapps.voicePDF.views.PageViewControls;

public class PDFViewerActivity extends Activity {

	private static final int STARTPAGE = 1;
	private static final float STARTZOOM = 1.0f;
	
	public static final float MIN_ZOOM = 0.25f;
	public static final float MAX_ZOOM = 3.0f;
	private static final float ZOOM_INCREMENT = 1.2f;

	private static final int VIEW_BUFFER_SIZE = 3;
	private static final int PAGE_CACHE_SIZE = 9;
	private static final int CACHE_END_BUFFER = 2;
	private static final int CACHE_FORWARD = 1;
	private static final int CACHE_BACKWARD = 2;
	
    public static final String EXTRA_PDFFILENAME = "net.sf.andpdf.extra.PDFFILENAME";
    public static final String EXTRA_SHOWIMAGES = "net.sf.andpdf.extra.SHOWIMAGES";
    public static final String EXTRA_ANTIALIAS = "net.sf.andpdf.extra.ANTIALIAS";
    public static final String EXTRA_USEFONTSUBSTITUTION = "net.sf.andpdf.extra.USEFONTSUBSTITUTION";
    public static final String EXTRA_KEEPCACHES = "net.sf.andpdf.extra.KEEPCACHES";
	
	public static final boolean DEFAULTSHOWIMAGES = true;
	public static final boolean DEFAULTANTIALIAS = true;
	public static final boolean DEFAULTUSEFONTSUBSTITUTION = false;
	public static final boolean DEFAULTKEEPCACHES = false;
	
	private SpeechRecognizer mSpeechRecognizer;
	
	private final GraphView[] viewBuffer = new GraphView[VIEW_BUFFER_SIZE];
	private PageViewControls mPageControlsView;
	private FrameLayout mView;
	private String pdffilename;
	private PDFFile mPdfFile;
	private float mZoom;

	private final ArrayList<PDFPage> pages = new ArrayList<PDFPage>();
    private PDFPage mPdfPage;
    private int currPage, oldPage = 0;
    private int cacheBegPage, cacheEndPage;
	private float pageWidth, pageHeight;
	private int zoomedWidth, zoomedHeight;

    private Thread backgroundThread;
    private Handler uiHandler;
    private boolean isListening = true;
	private boolean fromMainActivity = true;
    
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        uiHandler = new Handler(); 
        Intent intent = getIntent();

        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        CommandRecognitionListener mCommandListener = new CommandRecognitionListener();
        mSpeechRecognizer.setRecognitionListener(mCommandListener);
        //starts listening in startRenderThread()
        
        //initialize views for swapping
        int i;
        for (i=0; i<VIEW_BUFFER_SIZE;i++) {
        	viewBuffer[i] = new GraphView(this); 
        }
        mView = new FrameLayout(this);
        mPageControlsView = new PageViewControls(this);
        for (i=0; i<VIEW_BUFFER_SIZE;i++) {
            mView.addView(viewBuffer[i]);
        }
        mView.addView(mPageControlsView);
        
        boolean showImages = getIntent().getBooleanExtra(PDFViewerActivity.EXTRA_SHOWIMAGES, PDFViewerActivity.DEFAULTSHOWIMAGES);
        PDFImage.sShowImages = showImages;
        boolean antiAlias = getIntent().getBooleanExtra(PDFViewerActivity.EXTRA_ANTIALIAS, PDFViewerActivity.DEFAULTANTIALIAS);
        PDFPaint.s_doAntiAlias = antiAlias;
    	boolean useFontSubstitution = getIntent().getBooleanExtra(PDFViewerActivity.EXTRA_USEFONTSUBSTITUTION, PDFViewerActivity.DEFAULTUSEFONTSUBSTITUTION);
        PDFFont.sUseFontSubstitution= useFontSubstitution;
    	boolean keepCaches = getIntent().getBooleanExtra(PDFViewerActivity.EXTRA_KEEPCACHES, PDFViewerActivity.DEFAULTKEEPCACHES);
        HardReference.sKeepCaches= keepCaches;
	        
        if (intent != null) {
        	if ("android.intent.action.VIEW".equals(intent.getAction())) {
        		pdffilename = intent.getData().getPath();
        		fromMainActivity = false;
        	}
        	else {
                pdffilename = getIntent().getStringExtra(PDFViewerActivity.EXTRA_PDFFILENAME);
        	}
        }
        
        if (pdffilename == null)
        	pdffilename = "no file selected";

		currPage = cacheBegPage = STARTPAGE;
		mZoom = STARTZOOM;
		
		setContent(null);
    }
    	
	private void setContent(String password) {
        try { 
    		parsePDF(pdffilename, password);
	        setContentView(mView);
	        loadInitialPages();
    	}
        catch (PDFAuthenticationFailureException e) {
        	setContentView(R.layout.pdf_file_password);
           	final EditText etPW= (EditText) findViewById(R.id.etPassword);
           	Button btOK= (Button) findViewById(R.id.btOK);
        	Button btExit = (Button) findViewById(R.id.btExit);
            btOK.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					String pw = etPW.getText().toString();
		        	setContent(pw);
				}
			});
            btExit.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					finish();
				}
			});
        }
	}
	
	@SuppressWarnings("deprecation")
	private synchronized void loadInitialPages() {
		int numPages = mPdfFile.getNumPages();

		//get page size
		mPdfPage = mPdfFile.getPage(currPage, true);
		Display display = getWindowManager().getDefaultDisplay();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR2) { //set page width to display width
        	pageWidth = display.getWidth();
        }
        else {
	        Point size = new Point();
	        display.getSize(size);
	        pageWidth = size.x;
        }
        pageHeight = pageWidth/mPdfPage.getWidth()*mPdfPage.getHeight(); //scale page height relatively
		zoomedWidth = (int)pageWidth;
		zoomedHeight = (int)pageHeight;
		
        //load view buffer and page cache
        viewBuffer[currPage%VIEW_BUFFER_SIZE].PDFpage = mPdfPage;
        pages.add(mPdfPage);

        if (numPages > PAGE_CACHE_SIZE)
        	numPages = PAGE_CACHE_SIZE;
        
        for (int i=currPage+1; i<=numPages; i++) {
			mPdfPage = mPdfFile.getPage(i, true);
	        pages.add(mPdfPage);
        }

		cacheEndPage = numPages;
        startRender();
	}
	
	private void startRender() {
		if (mPdfFile != null) {
			showPage();
		}
		if (isListening)
			mSpeechRecognizer.startListening(RecognizerIntent.getVoiceDetailsIntent(this));
		mPageControlsView.redraw();
	}

	private synchronized void showPage() {
		//calculate zoom
		zoomedWidth = (int)(pageWidth*mZoom);
		zoomedHeight = (int)(pageHeight*mZoom);
		
        //update view
		uiHandler.post(new Runnable() {
			public void run() {
				viewBuffer[currPage%VIEW_BUFFER_SIZE].setPageBitmap(zoomedWidth, zoomedHeight);
				viewBuffer[currPage%VIEW_BUFFER_SIZE].updateImage();
				if (oldPage != currPage) {
					viewBuffer[currPage%VIEW_BUFFER_SIZE].setVisibility(true);
					viewBuffer[oldPage%VIEW_BUFFER_SIZE].setVisibility(false);
				}

				//synchronization issues
				if (backgroundThread != null) {
					while(backgroundThread != null) {
						; //wait
					}
				}
				
				//replaced by NEXT_VIEW
				if (currPage == oldPage+1 && currPage < mPdfFile.getNumPages()) {
					viewBuffer[(currPage+1)%VIEW_BUFFER_SIZE].PDFpage =  pages.get(currPage+1-cacheBegPage);
				}
				//replaced by PREVIOUS_VIEW
				else if (currPage == oldPage-1 && currPage > STARTPAGE) {
					viewBuffer[(currPage-1)%VIEW_BUFFER_SIZE].PDFpage =  pages.get(currPage-1-cacheBegPage);
				}
				
				if (currPage >= cacheEndPage-CACHE_END_BUFFER && cacheEndPage != mPdfFile.getNumPages()) {
					backgroundThread = new Thread(new Runnable() {
						public void run() {
							recachePages(CACHE_FORWARD);
					        backgroundThread = null;
						}
					});
			        backgroundThread.start();
				}
				else if (currPage <= cacheBegPage+CACHE_END_BUFFER && cacheBegPage != STARTPAGE) {
					backgroundThread = new Thread(new Runnable() {
						public void run() {
							recachePages(CACHE_BACKWARD);
					        backgroundThread = null;
						}
					});
			        backgroundThread.start();
				}
			}
		});
    }

	private void recachePages(int recache_type) {
		if (recache_type == CACHE_FORWARD) {
			int tempEnd = cacheEndPage-CACHE_END_BUFFER-CACHE_END_BUFFER+PAGE_CACHE_SIZE-1;
			//check if reached end of file
			if (tempEnd > mPdfFile.getNumPages()) {
				tempEnd = mPdfFile.getNumPages();
			}
			pages.subList(0,tempEnd-cacheEndPage).clear();
			for (int i=cacheEndPage+1;i<=tempEnd;i++) {
				mPdfPage = mPdfFile.getPage(i, true);
		        pages.add(mPdfPage);
			}
			cacheEndPage = tempEnd;
			cacheBegPage = cacheEndPage - PAGE_CACHE_SIZE+1;
		}
		else {
			int tempBeg = cacheBegPage+CACHE_END_BUFFER+CACHE_END_BUFFER+CACHE_END_BUFFER-PAGE_CACHE_SIZE+1;
			//check if reached beg of file
			if (tempBeg < STARTPAGE) {
				tempBeg = STARTPAGE;
			}

			pages.subList(PAGE_CACHE_SIZE-cacheBegPage+tempBeg,PAGE_CACHE_SIZE).clear();
			for (int i=cacheBegPage-1;i>=tempBeg;i--) {
				mPdfPage = mPdfFile.getPage(i, true);
		        pages.add(0,mPdfPage);
			}
			cacheBegPage = tempBeg;
			cacheEndPage = cacheBegPage + PAGE_CACHE_SIZE-1;
		}
	}
	
	public void toggleListen() {
		isListening = !isListening;
		if (isListening)
			mSpeechRecognizer.startListening(RecognizerIntent.getVoiceDetailsIntent(this));
		else
		    mSpeechRecognizer.stopListening();
	}
	
	public void toggleControls() {
		mPageControlsView.toggleZoomControls();
	}
	
	public float getZoom() {
		return mZoom;
	}
	
	public void setZoom(float zoom) {
		if (mPdfFile != null) {
			if (zoom > MAX_ZOOM)
				mZoom = MAX_ZOOM;
			else if (zoom < MIN_ZOOM)
				mZoom = MIN_ZOOM;
			mZoom = zoom;
			startRender();
		}
	}
	
    public void zoomIn() {
    	if (mPdfFile != null) {
    		if (mZoom < MAX_ZOOM) {
    			mZoom *= ZOOM_INCREMENT;
    			if (mZoom > MAX_ZOOM)
    				mZoom = MAX_ZOOM;
				
    			startRender();
    		}
    	}
	}

    public void zoomOut() {
    	if (mPdfFile != null) {
    		if (mZoom > MIN_ZOOM) {
    			mZoom /= ZOOM_INCREMENT;
    			if (mZoom < MIN_ZOOM)
    				mZoom = MIN_ZOOM;
    			
    			startRender();
    		}
    	}
	}

    public int getPageNum() {
    	return currPage;
    }
    
    public int getNumPages() {
    	if (mPdfFile != null) {
    		return mPdfFile.getNumPages();
    	}
    	return 0;
    }
    
    public void nextPage() {
    	if (mPdfFile != null) {
    		if (currPage < mPdfFile.getNumPages()) {
    			oldPage = currPage;
    			currPage++;
    			startRender();
    		}
    	}
	}

    public void prevPage() {
    	if (mPdfFile != null) {
    		if (currPage > 1) {
    			oldPage = currPage;
    			currPage--;
    			startRender();
    		}
    	}
	}
    
	public void gotoPage(int pageNum) {
		//TODO doesn't work as expected
		if ((pageNum!=currPage) && (pageNum>=1) && (pageNum <= mPdfFile.getNumPages())) {
			currPage = pageNum;
			//recache if necessary
			if (currPage < cacheBegPage || currPage > cacheEndPage) {
				recachePages(CACHE_FORWARD);
			}
			reinitializeViewBuffer();
			oldPage = currPage;
			startRender();
		}
	}
	
	private void reinitializeViewBuffer() {
        viewBuffer[currPage%VIEW_BUFFER_SIZE].PDFpage =  pages.get(currPage-cacheBegPage);
		viewBuffer[currPage%VIEW_BUFFER_SIZE].setPageBitmap(zoomedWidth, zoomedHeight);
		viewBuffer[currPage%VIEW_BUFFER_SIZE].updateImage();
		viewBuffer[oldPage%VIEW_BUFFER_SIZE].setVisibility(false);
		viewBuffer[currPage%VIEW_BUFFER_SIZE].setVisibility(true);
		if (currPage > STARTPAGE) {
			viewBuffer[(currPage-1)%VIEW_BUFFER_SIZE].PDFpage =  pages.get(currPage-1-cacheBegPage);
		}
		if (currPage < mPdfFile.getNumPages()) {
			viewBuffer[(currPage+1)%VIEW_BUFFER_SIZE].PDFpage =  pages.get(currPage+1-cacheBegPage);
		}
	}
    
	public int getPageWidth() {
		return zoomedWidth;
	}
	
	public int getPageHeight() {
		return zoomedHeight;
	}
	
	public int getScreenWidth() {
		return mView.getWidth();
	}
	
	public int getScreenHeight() {
		return mView.getHeight();
	}
    
    private void parsePDF(String filename, String password) throws PDFAuthenticationFailureException {
    	try {
        	File f = new File(filename);
        	long len = f.length();
        	if (len > 0) {
    	    	openFile(f, password);
        	}
    	}
        catch (PDFAuthenticationFailureException e) {
        	throw e; 
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
    
    /**
     * <p>Open a specific pdf file.  Creates a DocumentInfo from the file,
     * and opens that.</p>
     *
     * <p><b>Note:</b> Mapping the file locks the file until the PDFFile
     * is closed.</p>
     *
     * @param file the file to open
     * @throws IOException
     */
    public void openFile(File file, String password) throws IOException {
        // first open the file for random access
        RandomAccessFile raf = new RandomAccessFile(file, "r");

        // extract a file channel
        FileChannel channel = raf.getChannel();

        // now memory-map a byte-buffer
        ByteBuffer bb = ByteBuffer.NEW(channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size()));
        
        // create a PDFFile from the data
        if (password == null)
        	mPdfFile = new PDFFile(bb);
        else
        	mPdfFile = new PDFFile(bb, new PDFPassword(password));
    }

    private void exit() {
  	  mSpeechRecognizer.stopListening();
	  mSpeechRecognizer.cancel();
	  mSpeechRecognizer.destroy();
	  finish();
    }
    
    @Override
    public void onResume() {
    	if (mSpeechRecognizer == null) {
    		mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
    		CommandRecognitionListener mCommandListener = new CommandRecognitionListener();
    		mSpeechRecognizer.setRecognitionListener(mCommandListener);
    		if (isListening)
    			mSpeechRecognizer.startListening(RecognizerIntent.getVoiceDetailsIntent(this));
    	}
    	super.onResume();
    }
    
    @Override
    public void onPause() {
    	mSpeechRecognizer.stopListening();
    	mSpeechRecognizer.cancel();
    	mSpeechRecognizer.destroy();
    	mSpeechRecognizer = null;
    	super.onPause();
    }
    
	@Override
	public void onBackPressed() {
	    mSpeechRecognizer.stopListening();
	    mSpeechRecognizer.cancel();
	    mSpeechRecognizer.destroy();
	    if (fromMainActivity) {
	    	startActivity(new Intent(this, MainActivity.class));
	    	finish();
	    }
	    super.onBackPressed();
	}
    
	/** Holds the PDF image in a scrollable view**/
	private class GraphView extends FrameLayout {
    	private Bitmap mBi = null;
    	public PDFPage PDFpage = null;
    	private PDFImageView mImageView;
    	private boolean isVisible;
    	
        public GraphView(Context context) {
            super(context);
            setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
	        setBackgroundColor(Color.LTGRAY);
	       
            mImageView = new PDFImageView(context);
            mImageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            addView(mImageView);
	        updateImage(); 
	        
	        setVisibility(false);
        }

        private void setVisibility(boolean visibility) {
        	isVisible = visibility;
        	if (isVisible)
        		setVisibility(View.VISIBLE);
        	else
        		setVisibility(View.GONE);
        }
        
        private void updateImage() {
			mImageView.updateImage(mBi);
    	}

		private void setPageBitmap(int zoomedWidth, int zoomedHeight) {
			if (PDFpage != null)
				mBi = PDFpage.getImage(zoomedWidth, zoomedHeight, null, true, true);
			else
				mBi = null;
		}
		
    }
	
    /** RecognitionListener for voice commands **/
    private class CommandRecognitionListener implements RecognitionListener {
    	LinkedHashSet <String> mHashSet;
    	
    	@Override
    	public void onBeginningOfSpeech() {
   		 //do nothing
    	}
    	
    	@Override
    	public void onBufferReceived(byte[] buffer) {
   		 //do nothing
    	}
    	
    	@Override
    	 public void onEndOfSpeech() {
   		 //do nothing
    	 }
    	
    	 @Override
    	 public void onError(int error) {
    		 if ((error == SpeechRecognizer.ERROR_NO_MATCH)) {
    			 Toast.makeText(PDFViewerActivity.this,"Words not recognized. Try again.",Toast.LENGTH_SHORT).show();
    			 //restart listening
    			 if (isListening)
    				 mSpeechRecognizer.startListening(RecognizerIntent.getVoiceDetailsIntent(PDFViewerActivity.this));
    		 }
    		 else if ((error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT)) {
    			 //restart listening
    			 if (isListening)
    				 mSpeechRecognizer.startListening(RecognizerIntent.getVoiceDetailsIntent(PDFViewerActivity.this));
    		 }
    	 }
    	
    	 @Override
    	 public void onEvent(int eventType, Bundle params) {
    		 //do nothing
    	 }
    	
    	 @Override
    	 public void onPartialResults(Bundle partialResults) {
    		 //do nothing
    	 }
    	
    	 @Override
    	 public void onReadyForSpeech(Bundle params) {
    		 //do nothing
    	 }
    	 
    	
    	 @Override
    	 public void onResults(Bundle results) {
    		 mHashSet = new LinkedHashSet<String>(5);
    	     List<String> mResults = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
    		 if (mResults.size() > 5) {
    			 mResults = mResults.subList(0, 5);
             }
    		 for (int i = 0; i < mResults.size();i++ ) {
    			 mHashSet.add(mResults.get(i).toLowerCase());
    		 }
    		 recognizeCommand();
    	 }
    	
    	 private void recognizeCommand() {
    		 String temp;
    		 Iterator<String> itr = mHashSet.iterator();
    		 while(itr.hasNext()) {
    			 temp = itr.next();
    			 if(temp.contains("next")) {
    				 PDFViewerActivity.this.nextPage();
    				 return;
    			 }
    			 else if(temp.contains("back")||temp.contains("previous")) {
    				 PDFViewerActivity.this.prevPage();
    				 return;
    			 }
    			 else if(temp.contains("out")) {
					 PDFViewerActivity.this.zoomOut();
    				 return;
    			 }
    			 else if(temp.contains("in")) {
					 PDFViewerActivity.this.zoomIn();
    				 return;
    			 }
    			 else if(temp.contains("controls")||temp.contains("toggle")) {
					 PDFViewerActivity.this.toggleControls();
		    	     mSpeechRecognizer.startListening(RecognizerIntent.getVoiceDetailsIntent(PDFViewerActivity.this));
    				 return;
    			 }
    			 else if(temp.contains("goto")) {
    				 //try to match page number
    				 while (itr.hasNext()) {
    					 temp = itr.next();
    					 temp = temp.replace("page ","");
    					 try {
            				 PDFViewerActivity.this.gotoPage(Integer.parseInt(temp));
            				 return;
    					 } catch (NumberFormatException e) {
    						 //do nothing
    					 }
    				 }
    			 }
    			 else if(temp.contains("exit")) {
					 PDFViewerActivity.this.exit();
					 return;
    			 }
    		 }
    		 
    		 Toast.makeText(PDFViewerActivity.this,"No command matched. Try again.",Toast.LENGTH_SHORT).show();
    	     mSpeechRecognizer.startListening(RecognizerIntent.getVoiceDetailsIntent(PDFViewerActivity.this));
		}

		@Override
    	 public void onRmsChanged(float rmsdB) {
    		 //do nothing
    	 }
    }
}