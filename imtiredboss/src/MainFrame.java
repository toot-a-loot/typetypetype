import javax.swing.JFrame;
import javax.swing.JPanel;

public class MainFrame extends JFrame
 {  
    public MainFrame(JPanel initialPanel) 
    {
        setSize(720, 960);
        setUndecorated(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);

        add(initialPanel);
        setVisible(true);
    }

    public void switchPanel(JPanel newPanel) 
    {
        getContentPane().removeAll();
        add(newPanel);
        revalidate();
        repaint();
    }
}