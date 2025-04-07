public class Main {
    public static void main(String[] args) {
        MainMenu mainMenu = new MainMenu();
        MainFrame mainFrame = new MainFrame(mainMenu);

        WordGenerator wordGenerator = new WordGenerator("words.txt");
    }
}