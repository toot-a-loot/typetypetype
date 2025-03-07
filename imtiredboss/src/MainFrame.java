
import javax.swing.JFrame;
import javax.swing.JPanel;

public class MainFrame 
{
    private JFrame mainFrame;

    public MainFrame(JPanel currentState)
    {
        mainFrame = new JFrame();
        mainFrame.setSize(720,960);
        mainFrame.setUndecorated(true);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setLayout(null);

        mainFrame.add(currentState);
        mainFrame.setVisible(true);
    }
}
