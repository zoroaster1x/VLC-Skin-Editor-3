package io.github.zoroaster1x.vlcskin.app.panel;

import io.github.zoroaster1x.vlcskin.ai.AiAssistant;
import io.github.zoroaster1x.vlcskin.app.Studio;
import io.github.zoroaster1x.vlcskin.app.i18n.Messages;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 * Chat with an OpenAI compatible endpoint that can drive the same tools the
 * MCP server exposes. Unconfigured, it explains what it needs.
 */
public final class AiPanel extends JPanel {

    private final Studio studio;
    private final JTextArea transcript = new JTextArea();
    private final JTextArea input = new JTextArea(3, 20);
    private final JTextField baseUrl = new JTextField();
    private final JTextField model = new JTextField();
    private final JPasswordField key = new JPasswordField();
    private final JLabel state = new JLabel();
    private AiAssistant assistant;

    public AiPanel(Studio studio) {
        this.studio = studio;
        setLayout(new BorderLayout());

        transcript.setEditable(false);
        transcript.setLineWrap(true);
        transcript.setWrapStyleWord(true);
        transcript.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel settings = new JPanel();
        settings.setLayout(new BoxLayout(settings, BoxLayout.Y_AXIS));
        settings.setBorder(BorderFactory.createEmptyBorder(6, 8, 0, 8));
        baseUrl.setText(studio.settings().getAiBaseUrl());
        model.setText(studio.settings().getAiModel());
        baseUrl.setToolTipText(Messages.get("APP_AI_ENDPOINT_TIP", "For example https://api.openai.com/v1"));
        settings.add(labeled(Messages.get("APP_AI_ENDPOINT", "Endpoint"), baseUrl));
        settings.add(labeled(Messages.get("APP_AI_MODEL", "Model"), model));
        settings.add(labeled(Messages.get("APP_AI_API_KEY", "API key"), key));
        state.setForeground(Color.GRAY);
        state.setText(keyHint());
        settings.add(state);

        JPanel composer = new JPanel(new BorderLayout(4, 4));
        composer.setBorder(BorderFactory.createEmptyBorder(4, 8, 8, 8));
        JScrollPane inputScroll = new JScrollPane(input);
        inputScroll.setPreferredSize(new Dimension(100, 70));
        composer.add(inputScroll, BorderLayout.CENTER);
        JButton send = new JButton(Messages.get("APP_AI_SEND", "Send"));
        send.addActionListener(this::send);
        composer.add(send, BorderLayout.EAST);

        JPanel top = new JPanel(new BorderLayout());
        top.add(settings, BorderLayout.CENTER);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(transcript), BorderLayout.CENTER);
        add(composer, BorderLayout.SOUTH);
        updateState();
    }

    private JPanel labeled(String label, javax.swing.JComponent field) {
        JPanel row = new JPanel(new BorderLayout(6, 0));
        JLabel name = new JLabel(label);
        name.setPreferredSize(new Dimension(70, 22));
        row.add(name, BorderLayout.WEST);
        row.add(field, BorderLayout.CENTER);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        return row;
    }

    private String keyHint() {
        String env = studio.settings().getAiKeyEnv();
        String value = System.getenv(env);
        if (value != null && !value.isBlank()) {
            return Messages.format("APP_AI_KEY_AVAILABLE",
                    "A key is available from $%s; leaving the field empty uses it.", env);
        }
        return Messages.format("APP_AI_KEY_MISSING",
                "No key in $%s; paste one above (it is kept in memory only).", env);
    }

    private void updateState() {
        state.setText(keyHint());
    }

    private void send(ActionEvent event) {
        String message = input.getText().trim();
        if (message.isEmpty()) {
            return;
        }
        input.setText("");
        append(Messages.format("APP_AI_YOU", "you: %s", message));
        String apiKey = new String(key.getPassword());
        if (apiKey.isBlank()) {
            String env = studio.settings().getAiKeyEnv();
            apiKey = System.getenv(env) == null ? "" : System.getenv(env);
        }
        studio.settings().setAiBaseUrl(baseUrl.getText().trim());
        studio.settings().setAiModel(model.getText().trim());
        studio.saveSettings();
        AiAssistant.Config config = new AiAssistant.Config(baseUrl.getText().trim(), apiKey,
                model.getText().trim(), true);
        if (!config.usable()) {
            append(Messages.get("APP_AI_NOT_CONFIGURED",
                    "assistant: Set an endpoint and a model first. Any OpenAI compatible server works, "
                            + "for example a local llama.cpp or Ollama /v1 endpoint."));
            return;
        }
        assistant = new AiAssistant(studio.service(), config);
        Thread worker = new Thread(() -> {
            try {
                String answer = assistant.send(message);
                append(Messages.format("APP_AI_ASSISTANT", "assistant: %s", answer));
            } catch (Exception ex) {
                append(Messages.format("APP_AI_ASSISTANT", "assistant: %s", ex.getMessage()));
            }
            SwingUtilities.invokeLater(() -> studio.session().fireChanged());
        }, "vlc-skin-ai");
        worker.setDaemon(true);
        worker.start();
    }

    private void append(String text) {
        transcript.append(text + "\n\n");
        transcript.setCaretPosition(transcript.getDocument().getLength());
    }
}
