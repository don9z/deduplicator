import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static junit.framework.Assert.assertEquals;

public class DeduplicatorTest {

    @Test
    public void testRemoveDuplicates() {
        String[] contents = {
                "I love Emacs, since Emacs is awesome",
                "Emacs is awesome, I love it",
                "Oh my way home. Long day ahead",
                "It's a long day, I have to admit it",
                "Good artists copy, great artists steal",
                "Something interesting, Good artists copy, great artists steal",
                "Great artists steal, Good artists copy",
                "I really think Emacs is awesome, and love it"
        };
        List<String> contentList = Arrays.asList(contents);

        Deduplicator deDuplicator = new Deduplicator(contentList, 0.5f);
        List<String> results = deDuplicator.dedup();

        assertEquals(4, results.size());
        assertEquals(contentList.get(0), results.get(0));
        assertEquals(contentList.get(2), results.get(1));
        assertEquals(contentList.get(3), results.get(2));
        assertEquals(contentList.get(4), results.get(3));
    }
}
