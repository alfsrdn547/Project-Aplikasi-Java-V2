import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class UIHelper {
    private static final int ARC_RADIUS = 16;

    public static JButton createButton(String text, Color background, Color foreground) {
        JButton button = new JButton(text);
        applyStyle(button, background, foreground);
        return button;
    }

    public static JButton createPrimaryButton(String text) {
        return createButton(text, new Color(41, 128, 185), Color.WHITE);
    }

    public static JButton createSecondaryButton(String text) {
        return createButton(text, new Color(46, 204, 113), Color.WHITE);
    }

    public static JButton createAccentButton(String text) {
        return createButton(text, new Color(52, 152, 219), Color.WHITE);
    }

    public static JButton createDangerButton(String text) {
        return createButton(text, new Color(231, 76, 60), Color.WHITE);
    }

    public static JButton createNeutralButton(String text) {
        return createButton(text, new Color(149, 165, 166), Color.WHITE);
    }

    public static void applyStyle(JButton button, Color background, Color foreground) {
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setFocusPainted(false);
        button.setForeground(foreground);
        button.setBackground(background);
        button.setFont(new Font("Arial", Font.BOLD, 13));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(adjustColor(background, -0.18f), 1),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)
        ));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(adjustColor(background, 0.10f));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(background);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                button.setBackground(adjustColor(background, -0.08f));
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                button.setBackground(button.getModel().isRollover() ? adjustColor(background, 0.10f) : background);
            }
        });
    }

    private static Color adjustColor(Color color, float factor) {
        int r = Math.max(0, Math.min(255, (int) (color.getRed() * (1 + factor))));
        int g = Math.max(0, Math.min(255, (int) (color.getGreen() * (1 + factor))));
        int b = Math.max(0, Math.min(255, (int) (color.getBlue() * (1 + factor))));
        return new Color(r, g, b);
    }

    private static class RoundedBorder implements Border {
        private final Color color;
        private final int thickness;

        RoundedBorder(Color color, int thickness) {
            this.color = color;
            this.thickness = thickness;
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(thickness + 2, thickness + 2, thickness + 2, thickness + 2);
        }

        @Override
        public boolean isBorderOpaque() {
            return false;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setStroke(new BasicStroke(thickness));
            g2.setColor(color);
            g2.drawRoundRect(x + 1, y + 1, width - 3, height - 3, ARC_RADIUS, ARC_RADIUS);
            g2.dispose();
        }
    }
}
