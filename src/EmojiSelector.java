import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class EmojiSelector {
    public JPanel jPanel;
    private JScrollPane jScrollPane;
    public JList list;
    private JButton selectButton;

    public static int emoji = -1;


public EmojiSelector() {
    selectButton.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            emoji = list.getSelectedIndex();
        }
    });
}
}
