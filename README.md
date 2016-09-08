# PathDraw
It`s a android animation that drawing with svg .

**Thanks [https://github.com/geftimov/android-pathview](https://github.com/geftimov/android-pathview)**

this library is base on [android-pathview](https://github.com/geftimov/android-pathview) 

I removed the parallel animation and modified the fill animation .it will be like that：


  ![woman](https://github.com/MartinBZDQSM/PathDraw/blob/master/app/src/main/res/raw/woman.gif)

## sttr:
```
    <declare-styleable name="PathDrawingView">
        <attr name="path" format="reference"></attr>
        <attr name="draw_paint" format="boolean"></attr>
        <attr name="paint_color" format="color"></attr>
        <attr name="filling" format="boolean"></attr>
        <attr name="fill_after" format="boolean"></attr>
    </declare-styleable>
```
## Three modes

### 1.NORMAL

![ironman](https://github.com/MartinBZDQSM/PathDraw/blob/master/app/src/main/res/raw/ironman.gif)
 ```
     <com.martin.pdmaster.PathDrawingView
        android:id="@+id/pathdrawing"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:draw_paint="true"
        app:path="@raw/ironman"></com.martin.pdmaster.PathDrawingView>
 ```
 ### 2.FILL
 
![nancy](https://github.com/MartinBZDQSM/PathDraw/blob/master/app/src/main/res/raw/nancy.gif)
 ```
    <com.martin.pdmaster.PathDrawingView
        android:id="@+id/pathdrawing"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:draw_paint="true"
        app:fill_after="false"
        app:filling="true"
        app:paint_color="@color/colorPrimary"
        app:path="@raw/nancypath3"></com.martin.pdmaster.PathDrawingView>
 ```
 ### 3.AFTER FILL
 
![afterfill](https://github.com/MartinBZDQSM/PathDraw/blob/master/app/src/main/res/raw/afterfill.gif)
 ```
    <com.martin.pdmaster.PathDrawingView
        android:id="@+id/pathdrawing"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:draw_paint="true"
        app:fill_after="true"
        app:paint_color="#000000"
        app:path="@raw/monitor"></com.martin.pdmaster.PathDrawingView>
 ```
