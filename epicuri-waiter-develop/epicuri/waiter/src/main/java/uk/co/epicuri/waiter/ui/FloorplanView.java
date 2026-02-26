package uk.co.epicuri.waiter.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.view.View;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import uk.co.epicuri.waiter.R;
import uk.co.epicuri.waiter.interfaces.BitmapCallback;
import uk.co.epicuri.waiter.interfaces.OnTableChangeListener;
import uk.co.epicuri.waiter.model.EpicuriSessionDetail;
import uk.co.epicuri.waiter.model.EpicuriSessionDetail.State;
import uk.co.epicuri.waiter.model.EpicuriTable;
import uk.co.epicuri.waiter.utils.GlobalSettings;

public class FloorplanView extends View {

    static final long LONG_PRESS_THRESHOLD_MS = 2000;

	private enum EditMode {
        NONE, MOVE, ROTATE, RESIZE
    }

    /**
     * radius for touching - should be 48dp
     */
    private float touchRadius;

    private EditMode edit_changeMode = EditMode.NONE;

    private Paint bitmapPaint;
    private Paint background;
    private Paint fillPaint;
    private Paint borderPaint;
    private Paint textPaint;

    private Bitmap backgroundImage;
    private Bitmap rotateBitmap;
    private Bitmap moveBitmap;
    private Bitmap resizeBitmap;

    private Bitmap ic_zzz;
    private Bitmap ic_attn;
    private Bitmap ic_bill;

    private enum DrawState {VIEW, EDIT, TABLE_SELECT}

    private DrawState state = DrawState.VIEW;

    private float scaleFactorLimit = 1;
    private float scaleFactor = 1;
    private float topOffset = 0;
    private float leftOffset = 0;
    private float backgroundImageWidth;
    private float backgroundImageHeight;

    private Map<String,EpicuriSessionDetail> sessionsByTable = new HashMap<>();

    private int backgroundColor;
    private int emptyColor;
    private int busyColor;
    private int attnColor;
    private int soonColor;
    private int occupiedColor;
    private int busyBorderColor;
    private int highlightColor;
    private int selectableColor;
    private boolean showTableNames = true;

    private ArrayList<EpicuriTable> tables = new ArrayList<EpicuriTable>();
    private EpicuriTable selectedTable = null;
    private Map<String,Boolean> highlightedTables = null;
	private EpicuriSessionDetail sessionToReseat;
    private SharedPreferences prefs;

	private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector gestureDetector;

    private boolean isChanged = false;

    public boolean isChanged() {
        return isChanged;
    }

    public void markAsSaved() {
        isChanged = false;
    }

    public FloorplanView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        Resources res = context.getResources();
        highlightColor = res.getColor(R.color.blue);
        emptyColor = res.getColor(R.color.table_empty);
        busyColor = res.getColor(R.color.table_busy);
        occupiedColor = res.getColor(R.color.table_idle);
        busyBorderColor = res.getColor(R.color.table_busy_border);
        attnColor = res.getColor(R.color.table_attention);
        soonColor = res.getColor(R.color.table_soon);
        backgroundColor = res.getColor(R.color.darkgray);
        selectableColor = res.getColor(R.color.table_selectable);

        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setColor(0xff000000);
        borderPaint.setStrokeWidth(4);

        fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        fillPaint.setStyle(Paint.Style.FILL);

        bitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(0xff000000);
        textPaint.setTextSize(50);

        background = new Paint();
        background.setColor(res.getColor(R.color.darkgray));

        rotateBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.rotate);
        resizeBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.scale);
        moveBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.move);
        ic_zzz = BitmapFactory.decodeResource(getResources(), R.drawable.ic_time);
        ic_attn = BitmapFactory.decodeResource(getResources(), R.drawable.ic_alert);
        ic_bill = BitmapFactory.decodeResource(getResources(), R.drawable.ic_billindicator);


        touchRadius = rotateBitmap.getHeight();

        scaleGestureDetector = new ScaleGestureDetector(context, scaleGestureListener);
        gestureDetector = new GestureDetector(context, gestureListener);

        prefs = context.getSharedPreferences(GlobalSettings.PREF_APP_SETTINGS, Context.MODE_PRIVATE);
        prefs.registerOnSharedPreferenceChangeListener(tableTogglePreferenceChangeListener);
        showTableNames = prefs.getBoolean(GlobalSettings.PREF_KEY_SHOW_TABLE_NAME, true);
    }

    private String backgroundFilename = null;
    private boolean backgroundLoaded = false;

    public void setBackgroundFilename(final String url) {


        if (null != backgroundFilename && backgroundFilename.equals(url) && backgroundLoaded) {
            return; // don't reload if already shown
        }

        backgroundLoaded = false;
        backgroundFilename = url;


    }

    private void loadBackgroundImage() {
        backgroundLoaded = true;
        final FloorplanBackgroundCache cache = FloorplanBackgroundCache.getInstance(getContext());

        backgroundImage = cache.getCachedBitmap(backgroundFilename, getWidth(), getHeight());
        if (null != backgroundImage) {
            recalculateScaleFactor(getWidth(), getHeight());
            requestLayout();
        } else {
//			new AsyncTask<String, Void, Bitmap>() {
//
//				@Override
//				protected Bitmap doInBackground(String... params) {
//					try {
//						return cache.downloadAndCacheBitmap(params[0]);
//					} catch (ClientProtocolException e) {
//						e.printStackTrace();
//					} catch (IOException e) {
//						e.printStackTrace();
//					}
//					// error downloading or loading image
//					return null;
//				}
//
//				@Override
//				protected void onPostExecute(Bitmap result) {
//					backgroundImage = result;
//					recalculateScaleFactor(getWidth(), getHeight());
//					backgroundFilename = url;
//					requestLayout();
//				}
//
//			}.execute(url);

            try {
                cache.downloadAndCacheBitmap(backgroundFilename, new BitmapCallback() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap) {
                        backgroundImage = bitmap;
                        recalculateScaleFactor(getWidth(), getHeight());
                        requestLayout();
                    }
                }, getWidth(), getHeight());
            } catch (IOException e) {
                e.printStackTrace();
                backgroundImage = null;
                recalculateScaleFactor(getWidth(), getHeight());
                requestLayout();

            }
        }
    }

    public FloorplanView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FloorplanView(Context context) {
        this(context, null);
    }

    public void addTable(EpicuriTable table) {
        if (state != DrawState.EDIT) {
            throw new IllegalStateException("Cannot add table unless editing");
        }
        isChanged = true;
        EpicuriTable tableToRemove = null;
        for (EpicuriTable t : tables) {
            if (t.getId().equals(table.getId())) {
                tableToRemove = t;
                break;
            }
        }
        if (null != tableToRemove) {
            tableToRemove.setName(table.getName());
            tableToRemove.setShape(table.getShape());
        } else {
            selectedTable = table;

            float viewheight = scaleFactor * 800;
            float viewwidth = scaleFactor * 800 * backgroundImageWidth / backgroundImageHeight;

            int windowHeight = getHeight();
            int windowWidth = getWidth();

            // put table in middle of view
            selectedTable.translateTo(
                    ((leftOffset > 0 ? leftOffset : 0) + (viewwidth < windowWidth ? viewwidth : windowWidth) / 2) / scaleFactor,
                    ((topOffset > 0 ? topOffset : 0) + (viewheight < windowHeight ? viewheight : windowHeight) / 2) / scaleFactor);

            // if we already have some tables, set this new one to have the average dimensions
            if (!tables.isEmpty()) {
                float heights = 0;
                float widths = 0;
                int count = 0;
                for (EpicuriTable t : tables) {
                    heights += t.getHeight();
                    widths += t.getWidth();
                    count++;
                }
                selectedTable.scaleHeight(heights / count / 2);
                selectedTable.scaleWidth(widths / count / 2);
            }
            tables.add(selectedTable);
        }

        edit_changeMode = EditMode.MOVE;
        if (null != tableChangeListener) {
            tableChangeListener.onTableSelected(table.getId());
        }
        postInvalidate();
    }

    public void deleteTable(String id) {
        if (state != DrawState.EDIT) {
            throw new IllegalStateException("Cannot delete table unless editing");
        }
        EpicuriTable toDelete = null;
        isChanged = true;
        for (EpicuriTable t : tables) {
            if (t.getId().equals(id)) {
                toDelete = t;
            }
        }
        if (null == toDelete) {
            throw new IllegalArgumentException("Table not found");
        }

        tables.remove(toDelete);
        deselectTable();
        if (null != tableChangeListener) {
            tableChangeListener.onNoTableSelected();
        }
        invalidate();
    }

    public void deselectTable() {
        selectedTable = null;
        invalidate();
    }
//	public void selectTable(long tableId){
//		
//	}

    GestureDetector.OnGestureListener gestureListener = new GestureDetector.OnGestureListener() {

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }

        @Override
        public void onShowPress(MotionEvent e) {
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
                                float distanceY) {
            leftOffset += distanceX;
            topOffset += distanceY;
            resetBounds();
            postInvalidate();
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            // do nothing
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                               float velocityY) {
            // do nothing
            return false;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }
    };

    OnScaleGestureListener scaleGestureListener = new OnScaleGestureListener() {

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            if (scaleFactor < scaleFactorLimit) {
                scaleFactor = scaleFactorLimit;
            }
            postInvalidate();
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            return true;
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float oldScale = scaleFactor;
            scaleFactor *= detector.getScaleFactor();

            if (scaleFactor < scaleFactorLimit) {
                scaleFactor = scaleFactorLimit;
            }

            topOffset += detector.getFocusY() * (scaleFactor - oldScale);
            leftOffset += detector.getFocusX() * (scaleFactor - oldScale);

            postInvalidate();
            return true;
        }
    };

    // prevent scrolling beyond the bounds of the image
    private void resetBounds() {

        float viewheight = scaleFactor * 800;
        float viewwidth = scaleFactor * 800 * backgroundImageWidth / backgroundImageHeight;

        int windowWidth = getWidth();
        int windowHeight = getHeight();

        if (viewwidth < windowWidth) {
            leftOffset = -(windowWidth - viewwidth) / 2;
        } else if (leftOffset < 0) {
            leftOffset = 0;
        } else if (leftOffset > viewwidth - windowWidth) {
            leftOffset = viewwidth - windowWidth;
        }

        if (viewheight < windowHeight) {
            topOffset = -(windowHeight - viewheight) / 2;
        } else if (topOffset < 0) {
            topOffset = 0;
        } else if (topOffset > viewheight - windowHeight) {
            topOffset = viewheight - windowHeight;
        }


        postInvalidate();
    }

    final Runnable toggleTableNames = new Runnable() {
        @Override
        public void run() {
            // change the value then the listener will pick up the change
            prefs.edit().putBoolean(GlobalSettings.PREF_KEY_SHOW_TABLE_NAME, !showTableNames).apply();
        }
    };

    SharedPreferences.OnSharedPreferenceChangeListener tableTogglePreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if(key != null && key.equals(GlobalSettings.PREF_KEY_SHOW_TABLE_NAME)){
                showTableNames = sharedPreferences.getBoolean(GlobalSettings.PREF_KEY_SHOW_TABLE_NAME, true);
                invalidate();
            }
        }
    };

    Handler tableNameToggleHandler = new Handler();

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        tableNameToggleHandler.removeCallbacks(toggleTableNames);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(state != DrawState.EDIT){
            if(event.getActionMasked() == MotionEvent.ACTION_DOWN){
                tableNameToggleHandler.postDelayed(toggleTableNames, LONG_PRESS_THRESHOLD_MS);
            } else if (event.getActionMasked() == MotionEvent.ACTION_UP || event.getActionMasked() == MotionEvent.ACTION_CANCEL){
                tableNameToggleHandler.removeCallbacks(toggleTableNames);
                if(event.getEventTime() - event.getDownTime() > LONG_PRESS_THRESHOLD_MS){
                    // this has already triggered the toggle
                    return true;
                }
            }
        }
        if (tableMoveDetector(event)) {
            return true;
        }
        if (state != DrawState.EDIT) {
            return event.getActionMasked() == MotionEvent.ACTION_DOWN;
        }
        scaleGestureDetector.onTouchEvent(event);
        if (event.getActionMasked() == MotionEvent.ACTION_UP) {
            resetBounds();
        }
        //a lways propagage onDown
        if (event.getActionMasked() != MotionEvent.ACTION_DOWN && scaleGestureDetector.isInProgress()) {
            return true;
        }
        return gestureDetector.onTouchEvent(event);
    }

    private boolean tableMoveDetector(MotionEvent event) {

        float scaledX = (leftOffset + event.getX()) / scaleFactor;
        float scaledY = (topOffset + event.getY()) / scaleFactor;

        // Edit mode, and a table is selected
        if (state == DrawState.EDIT && null != selectedTable) {

            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN: {

                    int iconOffset = rotateBitmap.getWidth() / 2;

                    EditMode[] result = new EditMode[]{
                            EditMode.ROTATE,
                            EditMode.RESIZE,
                            EditMode.MOVE
                    };

                    PointF[] handles = selectedTable.getHandles();
                    edit_changeMode = EditMode.NONE;
                    double oldProximity = Double.MAX_VALUE;
                    for (int i = 0; i < result.length; i++) {
                        double dx = selectedTable.getX() + handles[i].x - scaledX + iconOffset;
                        double dy = selectedTable.getY() + handles[i].y - scaledY + iconOffset;
                        double newProximity = Math.sqrt(dx * dx + dy * dy);
                        if (newProximity < touchRadius && newProximity < oldProximity) {
                            edit_changeMode = result[i];
                            oldProximity = newProximity;
                        }
                    }
                    if (EditMode.NONE != edit_changeMode) {
                        isChanged = true;
                        // work out and store offset from user's touch angle and table rotation
                        postInvalidate();
                        return true;
                    } else {
                        selectedTable = null;
                        if (null != tableChangeListener) {
                            tableChangeListener.onNoTableSelected();
                        }
                        postInvalidate();
                        return false;
                    }
                }
                case MotionEvent.ACTION_MOVE: {
                    // TODO: all these calculations are hacked together til they worked. Please refactor if you can work it out.

                    double cosAngle = selectedTable.getCosAngle();
                    double sinAngle = selectedTable.getSinAngle();
                    switch (edit_changeMode) {
                        // dimensions.right * sinAngle + dimensions.top * cosAngle
                        case MOVE:
                            float viewheight = 800;
                            float viewwidth = 800 * backgroundImageWidth / backgroundImageHeight;

                            if (scaledX > viewwidth) {
                                scaledX = viewwidth;
                            } else if (scaledX < 0) {
                                scaledX = 0;
                            }

                            if (scaledY > viewheight) {
                                scaledY = viewheight;
                            } else if (scaledY < 0) {
                                scaledY = 0;
                            }

                            selectedTable.translateTo(scaledX, scaledY);
                            break;
                        case RESIZE:

                            double dy = scaledY - selectedTable.getY();
                            double dx = scaledX - selectedTable.getX();
                            double hyp = Math.sqrt((dx * dx) + (dy * dy));
                            double extraAngle = Math.atan2(dy, dx);
                            cosAngle = Math.cos(-extraAngle + selectedTable.getRotation() * Math.PI / 180);
                            sinAngle = Math.sin(-extraAngle + selectedTable.getRotation() * Math.PI / 180);

                            selectedTable.scaleHeight(Math.abs(hyp * sinAngle));
                            selectedTable.scaleWidth(Math.abs(hyp * cosAngle));
                            break;
                        case ROTATE: {
//					double extra = (scaledX - selectedTable.getX()) > 0 ? Math.PI : 0;
                            double radians = Math.atan2(scaledX - selectedTable.getX(), scaledY - selectedTable.getY());
                            double radiansOffset = Math.atan2(selectedTable.getWidth(), selectedTable.getHeight());

                            double rotation = (180d + 180d * (radiansOffset - radians) / Math.PI);
                            selectedTable.rotateTo(rotation);
                            break;
                        }
                        case NONE:
                            // do nothing
                            break;
                    }
//				Log.d("Table",String.format("dimensions: %fx%f rot: %f", selectedTable.getWidth(),selectedTable.getHeight(), selectedTable.getRotation()));
//				Log.d("Table", String.format("touch offset from centre %f,%f", (scaledX - selectedTable.getX()), (scaledY - selectedTable.getY())));
                    postInvalidate();
                    return true;
                }

            }
        }

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (state != DrawState.TABLE_SELECT) {
                    // clear selected tables
//				highlightedTables = new SparseBooleanArray();
                }
                boolean deselected = false;

                // table already selected
                double distance = -1;
                for (EpicuriTable f : tables) {
                    double thisDistance = Math.sqrt(((scaledX - f.getX()) * (scaledX - f.getX())) + ((scaledY - f.getY()) * (scaledY - f.getY())));
//				if(thisDistance < (f.awidth + f.abreadth/4) && (distance == -1 || thisDistance < distance)){
                    double estimatedRadius = (f.getWidth() + f.getHeight()) / 4;
                    if (thisDistance < estimatedRadius && (distance == -1 || thisDistance < distance)) {
                        selectedTable = f;
                        distance = thisDistance;
                    }
                }

                // a new table selected

                if (null != selectedTable) {
                    if (state == DrawState.TABLE_SELECT) {
                        // only act if you click on an unoccupied table
                        EpicuriSessionDetail session = sessionsByTable.get(selectedTable.getId());
                        if (null == session || session.getState() == State.EMPTY || session.equals(sessionToReseat)) {
                            if (highlightedTables.containsKey(selectedTable.getId()) && highlightedTables.get(selectedTable.getId())) {
                                highlightedTables.put(selectedTable.getId(), false);
                            } else {
                                highlightedTables.put(selectedTable.getId(), true);
                            }
                        }

                        if (null != tableChangeListener) {
                            boolean selected = false;
                            for (EpicuriTable table : tables) {
                                if(highlightedTables.get(table.getId()) == null ? false : highlightedTables.get(table.getId())) {
                                    selected = true;
                                    break;
                                }
                            }
                            tableChangeListener.onHighlightedTablesChanged(selected);
                        }
                        selectedTable = null;
                        postInvalidate();
                    }
                    postInvalidate();
                    return true;
                } else if (deselected) {
                    if (null != tableChangeListener) {
                        tableChangeListener.onNoTableSelected();
                    }
                } else if (!deselected) {
                    return false;
                }
                break;
            case MotionEvent.ACTION_CANCEL: {
                if (state == DrawState.VIEW) {
                    selectedTable = null;
                }
                postInvalidate();
            }
            case MotionEvent.ACTION_UP: {
                if (null != selectedTable && null != tableChangeListener) {
                    tableChangeListener.onTableSelected(selectedTable.getId());
                }
                if (state == DrawState.VIEW) {
                    selectedTable = null;
                }
                postInvalidate();
                break;
            }
        }

        return false;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (null == backgroundImage) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
        int parentHeight = MeasureSpec.getSize(heightMeasureSpec);
        MeasureSpec.getMode(widthMeasureSpec);
        MeasureSpec.getMode(heightMeasureSpec);

        // ask for as much as possible
        setMeasuredDimension(parentWidth, parentHeight);
        return;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        recalculateScaleFactor(w, h);
        super.onSizeChanged(w, h, oldw, oldh);

        if (w != 0 && h != 0) {
            loadBackgroundImage();
        }
    }

    private void recalculateScaleFactor(int w, int h) {
        if (w <= 0 || h <= 0) {
            return;
        }
        float windowWidth = w;
        float windowHeight = h;

        // always 800 units tall

        if (null == backgroundImage) {
            if (w > h) {
                scaleFactorLimit = scaleFactor = h / 800f;
            } else {
                scaleFactorLimit = scaleFactor = w / 800f;
            }
        } else {
            backgroundImageWidth = backgroundImage.getWidth();
            backgroundImageHeight = backgroundImage.getHeight();

            // background image is always 800 units high, scaled to whatever the width is
            imageScaleRect = new Rect(0, 0, (int) (800f * backgroundImageWidth / backgroundImageHeight), 800);

            if (backgroundImageWidth / backgroundImageHeight > windowWidth / windowHeight) {
                scaleFactorLimit = scaleFactor = windowWidth / imageScaleRect.width();
            } else {
                scaleFactorLimit = scaleFactor = windowHeight / imageScaleRect.height();
            }
        }
        resetBounds();
    }

    private Rect imageScaleRect;
    private Rect textBounds = new Rect();

    @Override
    protected void onDraw(Canvas canvas) {
        int iconOffset = 0;
        canvas.drawColor(backgroundColor);
        if (null != rotateBitmap) {
            // only relevant if in editmode
            iconOffset = rotateBitmap.getWidth() / 2;
        }
        canvas.translate(-leftOffset, -topOffset);
        canvas.save();
        canvas.scale(scaleFactor, scaleFactor);

        if (null != backgroundImage) {
            canvas.drawBitmap(backgroundImage, null, imageScaleRect, bitmapPaint);
        }

        for (EpicuriTable table : tables) {

            canvas.save();
            canvas.translate((float)table.getX(), (float)table.getY());

            canvas.rotate((float)table.getRotation());

            fillPaint.setColor(0x0000000);
            borderPaint.setStrokeWidth(4);
            borderPaint.setColor(0xff000000);

            EpicuriSessionDetail session = sessionsByTable.get(table.getId());
            Bitmap icon = null;
            if (state == DrawState.TABLE_SELECT) {
                if (highlightedTables.containsKey(table.getId()) && highlightedTables.get(table.getId())) {
                    fillPaint.setColor(highlightColor);
                } else if (null == session || session.equals(sessionToReseat)) {
	                fillPaint.setColor(selectableColor);
	                borderPaint.setStrokeWidth(8);
                } else {
                    switch (session.getState()) {
                        case ATTENTION:
                        case IDLE:
                        case SOON:
                            fillPaint.setColor(busyColor);
                            borderPaint.setColor(busyBorderColor);
                            borderPaint.setStrokeWidth(2);
                            break;
                        case EMPTY:
                            fillPaint.setColor(emptyColor);
                            break;
                    }
                }
            } else if (state == DrawState.EDIT) {
                if (selectedTable == table) {
                    fillPaint.setColor(highlightColor);
                } else if (null == session) {
                    fillPaint.setColor(emptyColor);
                } else {
                    switch (session.getState()) {
                        case ATTENTION:
                            fillPaint.setColor(attnColor);
                            break;
                        case IDLE:
                            fillPaint.setColor(occupiedColor);
                            break;
                        case EMPTY:
                            fillPaint.setColor(emptyColor);
                            break;
                        case SOON:
                            fillPaint.setColor(soonColor);
                            break;
                    }
                }
            } else {
                if (null == session) {
                    fillPaint.setColor(emptyColor);
                } else if (highlightedTables != null) {
                    if (highlightedTables.containsKey(table.getId()) && highlightedTables.get(table.getId())) {
                        fillPaint.setColor(highlightColor);
                    } else {
                        fillPaint.setColor(busyColor);
                    }
                } else {
                    switch (session.getState()) {
                        case ATTENTION:
                            fillPaint.setColor(attnColor);
                            break;
                        case IDLE:
                            fillPaint.setColor(occupiedColor);
                            break;
                        case EMPTY:
                            fillPaint.setColor(emptyColor);
                            break;
                        case SOON:
                            fillPaint.setColor(soonColor);
                            break;
                    }

                    switch (session.getIcon()) {
                        case ATTN:
                            icon = ic_attn;
                            break;
                        case BILL:
                            icon = ic_bill;
                            break;
                        case ZZZ:
                            icon = ic_zzz;
                            break;
                        default:
                            icon = null;
                            break;
                    }
                }
            }
            switch (table.getShape()) {
                case SQUARE:
                    canvas.drawRoundRect(table.getDimensions(), 20, 20, fillPaint);
                    canvas.drawRoundRect(table.getDimensions(), 20, 20, borderPaint);
                    break;
                case CIRCLE:
                    canvas.drawOval(table.getDimensions(), fillPaint);
                    canvas.drawOval(table.getDimensions(), borderPaint);
                    break;
            }
            if (null != icon) {
                canvas.scale(2, 2);
                canvas.drawBitmap(icon, -icon.getWidth() / 2, -icon.getHeight() / 2, null);
            }

            if (state == DrawState.EDIT
                    || showTableNames
                    || (state == DrawState.TABLE_SELECT && (null == session || session.getState() == State.EMPTY || session.equals(sessionToReseat)))) {
                // draw table name if in edit mode, if toggle is set, or if table is empty in select mode
                canvas.rotate((float)-table.getRotation());
                textPaint.getTextBounds(table.getName(), 0, table.getName().length(), textBounds);
                canvas.drawText(table.getName(), -textBounds.right / 2, textBounds.top - textPaint.ascent(), textPaint);
            }
            canvas.restore();
        }
        canvas.restore();

        if (state == DrawState.EDIT && null != selectedTable) {
            canvas.save();
            canvas.translate((float)(scaleFactor * selectedTable.getX()), (float)(scaleFactor * selectedTable.getY()));
            canvas.translate(-iconOffset, -iconOffset);

            PointF[] handles = selectedTable.getHandles();

            canvas.drawBitmap(rotateBitmap, scaleFactor * handles[0].x, scaleFactor * handles[0].y, bitmapPaint);

            canvas.drawBitmap(resizeBitmap, scaleFactor * handles[1].x, scaleFactor * handles[1].y, bitmapPaint);

            canvas.drawBitmap(moveBitmap, handles[2].x, handles[2].y, bitmapPaint);
        }

    }

    private OnTableChangeListener tableChangeListener = null;

    public void setOnTableChangeListener(OnTableChangeListener otcl) {
        tableChangeListener = otcl;
    }

    public void setIsEditable(boolean isEditable) {
        state = isEditable ? DrawState.EDIT : DrawState.VIEW;
    }

    public void setLayout(List<EpicuriTable> layout) {
        tables.clear();
        if (null == layout) {
            Log.d("FloorplanView", "Null layout");
        } else {
            for (EpicuriTable t : layout) {
                tables.add(t.cloneTable());
            }
        }
        postInvalidate();
    }

    public void setState(List<EpicuriSessionDetail> sessions) {
        sessionsByTable.clear();
        if (null != sessions) {
            for (EpicuriSessionDetail s : sessions) {
                if (s.getTables() != null) {
                    for (EpicuriTable table : s.getTables()) {
                        sessionsByTable.put(table.getId(), s);
                    }
                }
            }
        }
        postInvalidate();
    }

    public EpicuriTable getTable(String tableId) {
        for (EpicuriTable t : tables) {
            if (t.getId().equals(tableId)) {
                return t;
            }
        }
        return null;
    }

    public ArrayList<EpicuriTable> getTables() {
        return tables;
    }


    public boolean highlightTables(String[] tablesToHighlight) {
        boolean response = false;
        if (tablesToHighlight == null) {
            highlightedTables = null;
        } else {
            highlightedTables = new LinkedHashMap<>();

            for (int i = 0; i < tablesToHighlight.length; i++) {
                for (EpicuriTable t : tables) {
                    if (t.getId().equals(tablesToHighlight[i])) {
                        response = true;
                        break;
                    }
                }
                highlightedTables.put(tablesToHighlight[i], true);
            }
        }
        postInvalidate();
        return response;
    }


    @Override
    public Parcelable onSaveInstanceState() {
        // begin boilerplate code that allows parent classes to save state
        Parcelable superState = super.onSaveInstanceState();

        SavedState ss = new SavedState(superState);
        // end

        ss.highlightedTables = this.highlightedTables;
        ss.selectedTable = (null == this.selectedTable ? "-1" : this.selectedTable.getId());
        ss.isChanged = isChanged;
        ss.tables = tables;

        return ss;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        // begin boilerplate code so parent classes can restore state
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        // end

        this.highlightedTables = ss.highlightedTables;
        if (ss.selectedTable != null || !ss.selectedTable.equals("-1")) {
            for (EpicuriTable t : tables) {
                if (t.getId().equals(ss.selectedTable)) {
                    this.selectedTable = t;
                    break;
                }
            }
        }
        this.isChanged = ss.isChanged;
        this.tables = ss.tables;
    }

    static class SavedState extends BaseSavedState {
        String selectedTable;
        Map<String,Boolean> highlightedTables = null;
        boolean isChanged;
        ArrayList<EpicuriTable> tables;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            this.selectedTable = in.readString();
            in.readMap(this.highlightedTables, Map.class.getClassLoader());
            this.isChanged = in.readInt() == 1;

            Parcelable[] tableArray = in.readParcelableArray(EpicuriTable.class.getClassLoader());
            this.tables = new ArrayList<EpicuriTable>(tableArray.length);
            for (int i = 0; i < tableArray.length; i++) {
                tables.add((EpicuriTable) tableArray[i]);
            }
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeString(this.selectedTable);
            out.writeMap(highlightedTables);
            out.writeInt(isChanged ? 1 : 0);

            EpicuriTable[] tableArray = tables.toArray(new EpicuriTable[tables.size()]);
            out.writeParcelableArray(tableArray, 0);
        }

        // required field that makes Parcelables from a Parcel
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    public void setTableSelectionMode(boolean tableSelectMode) {
        if (tableSelectMode) {
            state = DrawState.TABLE_SELECT;
            if (null == highlightedTables) {
                highlightedTables = new LinkedHashMap<>();
            }
        } else {
            state = DrawState.VIEW;
            highlightedTables = null;
        }
        postInvalidate();
    }

	public void setTableSelectionMode(boolean tableSelectMode, EpicuriSessionDetail session) {
		sessionToReseat = session;
		setTableSelectionMode(tableSelectMode);
	}

    public Map<String,Boolean> getHighlightedTables() {
        if (null == highlightedTables) {
            return null;
        }
        return new LinkedHashMap<>(highlightedTables);
    }


}
