// P A C K A G E ///////////////////////////////////////////////////////////////////////////////////
package co.warizmi.passwordcard;

// I M P O R T /////////////////////////////////////////////////////////////////////////////////////
import static java.lang.System.*;
import static org.eclipse.swt.SWT.*;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Properties;

import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tray;
import org.eclipse.swt.widgets.TrayItem;

// C L A S S ///////////////////////////////////////////////////////////////////////////////////////
public class App {
    private static final String TRAY_IMAGE = "/icon.png";
    private static final String DEFAULT_PROPERTIES = "/passwordcard.properties";

    public static void main(String[] aArgs) {
        try {
            String config = DEFAULT_PROPERTIES;
            boolean html = false;
            boolean tray = false;

            for (String arg : aArgs)
                if (!arg.startsWith ("-"))
                    config = arg;
                else if (arg.equals ("--html"))
                    html = true;
                else if (arg.equals ("--tray"))
                    tray = true;
                else
                    throw new IllegalArgumentException (arg);

            if (tray) {
                new App (config).run ();
            }
            else {
                setOut (new PrintStream (new FileOutputStream (FileDescriptor.out), true, "UTF-8"));
                Card card = new Card (config);
                out.println (html? card.toHtml () : card.toString ());
            }
        }
        catch (Exception e) {
            err.println ("Unhandled exception. Closing application");
            e.printStackTrace ();
            exit (-1);
        }
    }

    Properties mConfig = new Properties ();
    Card mCard;

    Display mDisplay;
    Image mImage;
    TrayItem mCardTray;
    Shell mCardWindow;
    Browser mBrowser;

    private App (String aConfigProperties) throws IOException {
        super ();
        String props = aConfigProperties;
        try {
            // TODO Check property existence properly
            mConfig.load (new FileReader (aConfigProperties));
        }
        catch (IOException e) {
            InputStream stream = App.class.getResourceAsStream (DEFAULT_PROPERTIES);
            mConfig.load (new InputStreamReader (stream));
            props = DEFAULT_PROPERTIES;
        }

        mCard = new Card (props);
        mDisplay = new Display ();
        mCardTray = createTray ();
        mCardWindow = createWindow ();
        mBrowser = createBrowser ();
    }

    void toggleWindow () {
        if (mCardWindow.isVisible ()) {
            Shell wnd = mCardWindow;
            mCardWindow = createWindow ();
            mBrowser.setParent (mCardWindow);
            wnd.close ();
        }
        else {
            mCardWindow.open ();
        }
    }

    private Browser createBrowser () throws IOException {
        Browser browser = new Browser (mCardWindow, NONE);
        browser.setText (mCard.toHtml ());
        browser.setJavascriptEnabled (false);
        browser.setEnabled (false);
        return browser;
    }

    private TrayItem createTray () {
        final Tray tray = mDisplay.getSystemTray ();

        if (tray == null)
            throw new IllegalStateException ("The system tray is not available");

        mImage = new Image (mDisplay, App.class.getResourceAsStream (TRAY_IMAGE));


        final TrayItem item = new TrayItem (tray, NONE);
        item.setToolTipText ("Password Card Tray");
        item.setImage (mImage);

        item.addListener (Selection, new Listener () {
            @Override
            public void handleEvent (Event event) {
                toggleWindow ();
            }
        });

        item.addListener (DefaultSelection, new Listener () {
            @Override
            public void handleEvent (Event event) {
                item.dispose ();
            }
        });

        return item;
    }

    private Shell createWindow () {
        final Shell shell = new Shell (mDisplay, NO_TRIM | ON_TOP);
        shell.setLayout (new FillLayout ());
        shell.setAlpha (196);
        shell.setImage (mImage);
        shell.setText ("Password Card");
        shell.setSize (550, 250);
//        Region r = new Region ();
//        shell.setRegion (r);

        return shell;
    }

    private void run () {
        while (!mCardTray.isDisposed ())
            if (!mDisplay.readAndDispatch ())
                mDisplay.sleep ();

        mCardWindow.dispose ();
        mImage.dispose ();
        mDisplay.dispose ();
    }
}
// E O F ///////////////////////////////////////////////////////////////////////////////////////////