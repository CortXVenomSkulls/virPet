import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Loads {@code event_cards.cards} (line-oriented text format). */
public final class EventCardLoader {

    private EventCardLoader() {
    }

    // bu method dosya okuma hatası fırlatabilir diyor throws IOexception derken
    public static List<EventCard> load(Path path) throws IOException {
        List<EventCard> out = new ArrayList<>();
        // Reader isminde bir BufferReader oluşturduk. ilk parametre olarak fonksiyona
        // girilen dosya konumunu (even_cards.cards) verdik ikinci olarak da standart
        // karakter serisi olan utf8'i verdik
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            String cardId = null;
            StringBuilder text = null;
            StatDelta yes = StatDelta.ZERO;
            StatDelta no = StatDelta.ZERO;
            boolean inText = false;

            // dosyadan tek tek satırları okur
            while ((line = reader.readLine()) != null) {
                String t = line.trim();
                if (t.isEmpty() || t.startsWith("#")) {
                    continue;
                }
                if (t.startsWith("card ")) {
                    // cardı gördüysek default bir card oluşturduk bomboş
                    cardId = t.substring(5).trim();
                    text = null;
                    yes = StatDelta.ZERO;
                    no = StatDelta.ZERO;
                    inText = false;
                } else if ("text".equals(t)) {
                    inText = true;
                    text = new StringBuilder();
                } else if ("end".equals(t)) {
                    out.add(new EventCard(cardId, text.toString(), yes, no));
                    cardId = null;
                    inText = false;
                } else if (t.startsWith("yes")) {
                    inText = false;
                    yes = parseDelta(t.substring(3).trim());
                } else if (t.startsWith("no")) {
                    inText = false;
                    no = parseDelta(t.substring(2).trim());
                } else if (inText) {
                    if (text.length() > 0) {
                        text.append('\n');
                    }
                    text.append(line);
                }
            }
        }
        return out;
    }

    private static StatDelta parseDelta(String rest) {
        int hunger = 0, affection = 0, weight = 0, health = 0;
        if (rest.isEmpty()) {
            return StatDelta.ZERO;
        }
        // \\s+ boşluk demek, boşluklardan parçalara böldük, her parçaya token
        for (String token : rest.split("\\s+")) {
            // soru işaretinin indeksi (yeri)
            int eq = token.indexOf('=');
            if (eq < 1) {
                continue;
            }
            // soru işaretinden bir sonraki karakteri integera dçnüştürüyor bunun adı da v
            int v = Integer.parseInt(token.substring(eq + 1));
            switch (token.substring(0, eq)) {
                case "hunger":
                    hunger = v;
                    break;
                case "affection":
                    affection = v;
                    break;
                case "weight":
                    weight = v;
                    break;
                case "health":
                    health = v;
                    break;
                default:
                    break;
            }
        }
        return new StatDelta(hunger, affection, weight, health);
    }
}
