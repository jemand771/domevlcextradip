package net.jemand771.domevlcextradip;

import java.util.Arrays;

public class TestRect {


    public static void main(String[] args) {

        String windowName = "TeamSpeak 3";
        int[] rect;
        try {
            rect = GetWindowRect.getRect(windowName);
            System.out.printf("The corner locations for the window \"%s\" are %s",
                    windowName, Arrays.toString(rect));
        } catch (GetWindowRect.WindowNotFoundException e) {
            e.printStackTrace();
        } catch (GetWindowRect.GetWindowRectException e) {
            e.printStackTrace();
        }
    }
}
