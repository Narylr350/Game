package client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.TreeSet;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Player {
    private String name;
    private TreeSet<Integer> card;
}
