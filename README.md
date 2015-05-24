# buffaloe

How many buffalo does it take to make a valid sentence?

<img src="screenshot.png"></img>

Based on my friend Avery Katko's Prolog grammar [buffalo.pl](https://github.com/averykatko/buffalo).

It's written in ClojureScript with the Om library. I'll write more about this later, but there are two backend logic engines (you can choose in the upper-right):

1. core.logic: I [rewrote buffalo.pl using core.logic's dcg package](src/buffaloe/grammar.cljs). (Actually, I had to use a [random fork](https://github.com/aamedina/cljs.core.logic) of core.logic which brings DCGs and some other stuff over to ClojureScript from the Clojure version.) Then I just call it with your input, get a parse tree, and [graph that](src/buffaloe/graph.cljs). (I wrote the [dagre-react library](https://github.com/osnr/dagre-react) to do my layout for me while sticking to the Om/React rendering model.)

2. Prolog: Yes, this is actually a Prolog program running in your browser. No server at all. I [slightly modified](prolog/buffalo.pl) the original Prolog DCG code to cut out the tree drawing stuff, to reimplement some SWI Prolog standard library functions, and to accept input as command-line arguments.

    I came up with a [ridiculous build process](prolog/Makefile): I used [?-Prolog](http://www.call-with-current-continuation.org/prolog/README.html) to compile the Prolog to an essentially pure C program. Then I used [Emscripten](https://kripken.github.io/emscripten-site/) to compile `buffalo.c` to a JavaScript function; I take what you type in, run that function `buffalo` with your input as arguments, then intercept whatever `buffalo` prints to stdout and stderr and [parse that back into ClojureScript data structures](src/buffaloe/prolog.cljs), then graph that as I would with the core.logic output.

    It should be straightforward to switch buffalo.pl out for some other Prolog program and compile, so now you can turn anything that ?-Prolog accepts into a nice Web interface with a tree renderer.

## Known issues

- The program only gives you the first parse it can find at the moment.
- There are probably parse bugs and inconsistencies between the two engines.
- Advanced optimizations break the core.logic engine for some reason.
