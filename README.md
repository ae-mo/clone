

# Clone

Clone is a small web application to manage KSF Media user profiles. It was developed following the guidelines of the [KSF Media Code Test](https://gist.github.com/f-f/d09d10d0e1b2a81cc035eb35bb00d958).

## Overview

It is a single page application allowing the user to login and display their profile information. In addition, the user can edit their address.

It is a Clojure project, using [leiningen](https://github.com/technomancy/leiningen) and [shadow-cljs](https://github.com/thheller/shadow-cljs) as build tools.

On the frontend, it uses [Reagent](https://github.com/reagent-project/reagent), a ClojureScript interface to the [React](http://facebook.github.io/react/) framework.

The project was developed starting from [this template](https://github.com/reagent-project/reagent-template).

## Running

### Requirements

- [leiningen](https://github.com/technomancy/leiningen) (v2.x)
- [node.js](https://nodejs.org/) (v6.0.0+)
- [npm](https://www.npmjs.com/) (comes bundled with `node.js`)
- [Java SDK](https://adoptopenjdk.net/) (Version 8+)

### Development mode

To start the `shadow-cljs` compiler run
```
lein run -m shadow.cljs.devtools.cli watch app
```

The application will now be available at [http://localhost:3000](http://localhost:3000).

### Style compilation
To compile [less](https://github.com/Deraen/less4clj) sources and then watch for changes and recompile until interrupted, run
```
lein less4j auto
```

### Optional development tools

Start the browser REPL:

```
$ lein repl
```
The Jetty server can be started by running:

```clojure
(start-server)
```
and stopped by running:
```clojure
(stop-server)
```


### Building for release

```
lein do clean, uberjar
```

## Architecture

The application is a single page application (SPA).

The server supports the `/` route as the only entry point to the application (although it would be better to have a separate login page, see below).
The relevant files for the backend part are in the `src/clj/clone` directory.

The application integrates with the [Persona API](https://persona.api.ksfmedia.fi/) from KSF Media, making calls to it directly from the client. It was developed using `Reagent`.
The relevant files for the frontend part are in the `src/cljs/clone` directory.

## Tradeoffs and assumptions

This section documents the design choices that were adopted during development.

### Architecture

Going for a SPA seemed the most natural choice, given that I needed to integrate with an external API. Also, as I thought that securing the app was out of scope (see below), I did not provide a backend, thus the API calls are performed from the client.

Regarding the frontend structure, I basically created a component for each of the two pages of the app, with common header and footer components for all the pages.
Each component has its own state, apart from shared session data. It could have probably been possible to have a flattened shared state among all the application components that would have allowed me to spare some API calls, but I just thought it wasn't worth it for a simple two-page app.

For the rest, the architecture was pretty much dictated by the template I used.

### UI

In order to follow [KSF Media style guide](https://www.hbl.fi/styleguide-2/), I decided to use the main stylesheets from the [HBL](https://www.hbl.fi/) website as a basis. I also borrowed from [Mitt Konto](https://konto.ksfmedia.fi/).

The testing on different devices was not very extensive (just on my PC, smartphone and tablet), nonetheless the application should be easily accessible in general, thanks to the reuse of HBL stylesheets.

### Security

Apart from checking login credentials, I basically decided to give up securing the application, as doing so would have easily led me out of scope.
In particular:
- the login page is in the SPA, but it would be better to have it as a separate page and serve the SPA only _after_ authentication. This is to prevent unauthorized users from accessing sensible content;
- the access token provided by the persona API is stored in local storage, which is not recommended for a number of reasons (see [here](https://auth0.com/docs/security/store-tokens#don-t-store-tokens-in-local-storage)). There are a few solutions to this, the most popular probably being to use cookies, but they all require to do some work in the backend. This would lead to make the calls to the Persona API from the server and the overall architecture would be too much of an effort for the purpose of the app;
- Oauth suggests to store the access token in memory when there is no backend ([source](https://auth0.com/docs/security/store-tokens#don-t-store-tokens-in-local-storage)), but I felt that this would have impaired user experience (once you close the site you have to login again), so I decided to stick to local storage;
- The compiled jar itself does not support HTTPS, but this is not out of lazyness. In fact, it is common practice for Clojure web applications to use a separate server as a reverse proxy (see for instance [this stackoverflow question](https://stackoverflow.com/questions/24897037/clojure-compojure-ringand-https), but it is also [the approach used by heroku](https://stackoverflow.com/a/30697601)). The proxy is the one with HTTPS support. So one additional step would be to setup a reverse proxy, or one could simply deploy to a platform which automatically provides it (like I did). One could also try to provide HTTPS support from within the application, but this is not very well documented and probably less easy.
