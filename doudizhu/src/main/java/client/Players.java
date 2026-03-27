package client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Players {
    private String name;
    private List<Integer> card;

    public void setName(String name) {
        this.name = name;
    }

    public void setCard(List<Integer> card) {
        this.card = card;
    }

    public String getName() {
        return name;
    }

    public List<Integer> getCard() {
        return card;
    }
}
