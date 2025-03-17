public class Main {
    public static void main(String[] args) {
        MainMenu mainMenu = new MainMenu();
        Play play = new Play();
        MainFrame mainFrame = new MainFrame(play);

        WordGenerator wordGenerator = new WordGenerator("words.txt");
        WordThread wordThread = new WordThread(play, 5, wordGenerator);
        play.setWordThread(wordThread); 
        wordThread.start();
    }
}