# Welcome to the Non-Dairy Soy plugin!
[![Build Status](https://travis-ci.org/Non-Dairy-Soy-Plugin/Non-Dairy-Soy-Plugin.svg?branch=master)](https://travis-ci.org/Non-Dairy-Soy-Plugin/Non-Dairy-Soy-Plugin)

This document is subject to change, particularly at any point it is deemed
necessary to add or revise something.

## Project Goals

* Develop a top-quality IntelliJ language plugin
* Enhance the usability and maintainability of soy files
* Leverage the most powerful features of IntelliJ IDEA to promote a superior
  developer experience
* I18N support (still looking for translators)
* Automated tests designed to ensure a quality release every time
== Build

## Build
```bash
./gradlew buildPlugin
```

## Run
```bash
./gradlew runIdea
```


## FAQ

### Why am I seeing errors in IntelliJ?

> Well, considering the complexity of parsing Closure templates, and this
> project does not use the closure compiler to perform parsing, it is entirely
> likely that I missed something.
>
> First, be sure you are running the latest version of the plugin. I
> deliberately prioritize fixing parser errors above all other bugs and feature
> improvements. My coworkers use the plugin daily and I consider myself
> accountable if they loose productivity on account of the plugin.
>
> If you find that your bug is genuine, please report it at
> [Non_Dairy issue page](https://github.com/Non-Dairy-Soy-Plugin/Non-Dairy-Soy-Plugin/issues)
> and I'll get to it as soon as I can.
>
> Please include:
> * A description of the problem
> * A stack trace (if available) or a screen shot
> * Steps to reproduce the error
> * A sample source file if possible


### Can you add *such-and-such* feature?

> There is a fairly exhaustive list of things I'd like the plugin to do at some
> point, but I'm one guy with a full-time job and a family. "We get there, when
> we get there."
>
> The *big list* is on
> [GitHub](https://github.com/Non-Dairy-Soy-Plugin/Non-Dairy-Soy-Plugin/blob/master/IdLikeToHave.txt).
> Additional ideas are welcome. Prioritization is tracked on little yellow
> sticky notes and entirely subject to available time, feasibility, ROI, and public demand.

### Why a Plugin for Closure Templates?

> Simply put, our team started using Closure Templates in our project because
> they are nifty. As an avid IntelliJ IDEA user in an IntelliJ IDEA shop, I
> was disappointed there wasn't one already... so I built one.

&nbsp;
