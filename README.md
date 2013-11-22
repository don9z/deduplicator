# DeDuplicator #

Use Lucene to remove similar sentences.

Inputs:


```
    "I love Emacs, since Emacs is awesome",
    "Emacs is awesome, I love it",
    "Oh my way home. Long day ahead",
    "It's a long day, I have to admit it",
    "Good artists copy, great artists steal",
    "Something interesting, Good artists copy, great artists steal",
    "Great artists steal, Good artists copy",
    "I really think Emacs is awesome, and love it"
```

Outputs:

```
    "I love Emacs, since Emacs is awesome",
    "Oh my way home. Long day ahead",
    "It's a long day, I have to admit it",
    "Good artists copy, great artists steal",
```
