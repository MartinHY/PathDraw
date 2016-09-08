# PathDraw
It`s a android animation that drawing with svg .

**Thanks [https://github.com/geftimov/android-pathview](https://github.com/geftimov/android-pathview)**

this library is base on [android-pathview](https://github.com/geftimov/android-pathview) 

I removed the parallel animation and modified the fill animation .it will be like that：


  ![woman](https://github.com/MartinBZDQSM/PathDraw/blob/master/app/src/main/res/raw/woman.gif)

## **sttr:**
```
    <declare-styleable name="PathDrawingView">
        <attr name="path" format="reference"></attr>
        <attr name="draw_paint" format="boolean"></attr>
        <attr name="paint_color" format="color"></attr>
        <attr name="filling" format="boolean"></attr>
        <attr name="fill_after" format="boolean"></attr>
    </declare-styleable>
```
## **Three modes**

### 1.NORMAL

![ironman](https://github.com/MartinBZDQSM/PathDraw/blob/master/app/src/main/res/raw/ironman.gif)
add the code in your xml:
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
add the code in your xml:
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
add the code in your xml:
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
 
## **ADD THE LIBRARY **
 
Add it in your root build.gradle at the end of repositories:

	allprojects {
		repositories {
			...
			maven { url "https://jitpack.io" }
		}
	}
Step 2. Add the dependency

	dependencies {
	      compile 'com.github.MartinBZDQSM:PathDraw:1.0'
	}
 
##[BLOG]()

##**License**

```license
Copyright [2016] [MartinBZDQSM of copyright owner]

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
