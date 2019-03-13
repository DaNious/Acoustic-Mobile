# Acoustic-Mobile
Acoustic health sensing for mobile phones

# Modified 02/20/2019
add function of phone orientation indicator

# Modified 02/21/2019
1) rotation .txt also records the time stamp 
2) add vibrator indicating bad orientation
3) add a Togglebutton to start calibrate the orientation

# Modified 03/04/2019
1) rotation.txt and acceleration.txt record the orientation and acceleration readings
2) add a switch to configure the option of saving IMU readings

# Modified 03/12/2019
1) add a button to go through a complete workout automatically, which is able to make segmentation easy.
2) the steps are relax, hold, lift up, hold up and put back. (Time might change)
3) a progress dialog will show as instruction

# Modified 03/13/2019
1) record singly buttons are hidden from UI and UI are reassigned.
2) add a editText to let user input the name id.
3) add a counter and reset button to record the workout times for one run of the App.
4) add a delete button to delete previous file generated. (roll back once, but only PCM is covered for this time).
5) change some control's Enable status to avoid unnecessary touch.