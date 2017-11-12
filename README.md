# Styled NumberPicker

Fully customizable NumberPicker for Android, in iOS style.
Stock Android NumberPicker cannot be properly customized. This library should do pretty much everything a NumberPicker is expected to.

## Include in your Project

Add Styled NumberPicker to the dependencies in your app's build.gradle file

```
compile 'com.github.benitoborriello:StyledNumberPicker:1.0.0'
```

## Usage

Add Styled NumberPicker to your XML layout file

```
<com.benitoborriello.android.stylednumberpicker.StyledNumberPicker
	android:id="@+id/mStyledNumberPicker"
	android:layout_width="match_parent"
	android:layout_height="100dp"
	android:layout_marginTop="4dp"
	android:background="@color/colorPrimary"
	/>
```

## Setting values

In layout

```
app:minValue="0"
app:maxValue="10"
```

Programmatically

```
mStyledNumberPicker.setMinValue(0);
mStyledNumberPicker.setMaxValue(10);
```

## Setting text size, color and padding

In layout

```
app:textColor="@color/colorAccent"
app:textSize="20dp"
app:textPadding="6dp"
```

Programmatically

```
int textColor = getResources().getDrawable(R.color.colorWhite);
mStyledNumberPicker.setTextColor(textColor);

mStyledNumberPicker.setTextSize(30);
mStyledNumberPicker.setTextPadding(10);
```

### Dp or Px?

All values passed programmatically are in px. If you want to use dp instead of px, Styled NumberPicker provides a conversion method.

```
mStyledNumberPicker.setTextSize( mStyledNumberPicker.convertDpToPx(10) );
```


## Setting selected item borders

By default, the selected item is whitin 2 horizontal white lines, automatically. To change it:

In layout

```
app:selectedBordersBackground="@colors/colorBlack"
```

or

```
app:selectedBordersBackground="#000000"
```

Programmatically

```
Drawable selectedItemBorder = getResources().getDrawable(R.color.colorBlack);
mStyledNumberPicker.setSelectedBordersBackground(selectedItemBorder);
```

### Removing selected item borders

To remove the said lines, just set selectedBordersBackground to transparent (e.g. #00000000).


## Setting top and bottom gradient resources

Top and bottom gradients can result in a nice 3D effect. By default the gradients are transparent->white. The default height is 40dp for each gradient.

To specify different gradients, first create 2 XML drawable files like the following ones

drawable/gradient_top.xml
```
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android">
    <gradient
        android:startColor="#00000000"
        android:endColor="#FF000000"
        android:type="linear"
        android:angle="90" />
</shape>
```

drawable/gradient_bottom.xml
```
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android">
    <gradient
        android:startColor="#FF000000"
        android:endColor="#00000000"
        android:type="linear"
        android:angle="90" />
</shape>
```

Then, in layout

```
app:topGradientBackground="@drawable/gradient_top"
app:bottomGradientBackground="@drawable/gradient_bottom"
```

Or programmatically

```
Drawable topGradientBackground = getResources().getDrawable(R.drawable.gradient_top);
mStyledNumberPicker.setTopGradientBackground(topGradientBackground);

Drawable bottomGradientBackground = getResources().getDrawable(R.drawable.gradient_bottom);
mStyledNumberPicker.setBottomGradientBackground(bottomGradientBackground);
```

## Scrolling sound

The "CLICK" scrolling sound effect can be easily disabled

In layout

```
app:scrollSound="false"
```

Programmatically

```
mStyledNumberPicker.scrollingSoundEnabled(false);
```

### Defining a different sound

Further version of this library will let you choose your own scrolling sound effect. This feature has not been implemented yet.

## Event listener

The onChange event is triggered whenever a number is picked by the user or programmatically selected.

### Setting the onChangeListener

```
mStyledNumberPicker.setOnChangeListener( new StyledNumberPicker.onChangeListener(){

	@Override
	public void onChange(int currentValue, int currentPosition) {
		messageTextView.setText("Selected number: " + String.valueOf(currentValue));
	}
});
```

## Sample app

A sample application is included in the project. Just clone this repository and open with Android Studio

## Authors

* **Benito Borriello** - *Development* - [BenitoBorriello](https://github.com/benitoborriello)

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details

