# PassGen

![logo](https://github.com/MartinWie/PassGen/blob/master/logo.png)

A small tool to eliminate some personal pain points build with Kotlin + HTMX and Tailwind/daisy-ui

### Install tooling:

Tailwind: for CSS generation
DaisyUi: for simpler base styling
Browser-sync: auto refresh browser on changes, for details checkout 'startServer.sh'
Entr: trigger action on file change, for details checkout 'startServer.sh'
Aenv: load app environment varibles form the aws parameter store

```Terminal
npm install tailwindcss@latest @tailwindcss/cli@latest daisyui@latest
npm install -g browser-sync 
brew install entr
pip install aenv
```

### Live development

Run the following command/script

```Terminal
bash buildAndReloadBrowserOnsave.sh
```

For details please read the scripts, but here is the sort summary:
The script uses entr to trigger another script on every *.kt, *.js or *.svg file change.
(entr will exit if new files are created, this is the reason for the loop, for details read the man page of entr)
The second script (start server) builds our CSS and runs the app server with gradle.
After a certain log stage is reached the script starts/refreshes a browser session with browser-sync to live update the
page.
If startServer script receives an user exit, we run a stop script and break out of the loop to exit the
buildAndReloadBrowserOnsave.sh script.

### Build and run the project

#### Setup local postgres

Build the Docker image: Open a terminal and navigate to the directory containing the Dockerfile_Postgres. Then run the
following command:

```Terminal
docker build -t passgen-postgres -f Dockerfile_Postgres .
```

Run the Docker container: After the image is built, you can run a container from it using the following command:

```Terminal
docker run -d --name passgen-postgres -p 5432:5432 passgen-postgres
```

#### Build project

Trigger a full build(currently migration scripts are only executed here, so do not forget to run this for the respective
environment):

```Terminal
bash fullBuild.sh
```

Run the respective jar:

```Terminal
java -jar build/libs/de.mw.passgen-all.jar 
```

### Deploy to prod

Run migrations and build current version:

```Terminal
aenv -e Prod -s Passgen bash fullBuild.sh
```

### Rate limiting

Per-IP rate limiting is applied to all share and generation endpoints using Ktor's built-in `RateLimit` plugin (token
bucket algorithm). The limits are:

| Tier           | Limit         | Endpoints                                                                  |
|----------------|---------------|----------------------------------------------------------------------------|
| CREATE_SHARE   | 10 req / 60s  | `POST /share`, `POST /key/share`                                           |
| COMPLETE_SHARE | 5 req / 60s   | `POST /key/share/{id}/complete`                                            |
| VIEW_SHARE     | 30 req / 60s  | `GET /share/{id}/{salt}`, `POST /share/{id}/{salt}`, `GET /key/share/{id}` |
| GENERATE       | 120 req / 60s | `GET /word`                                                                |

When a client exceeds the limit they receive HTTP 429 with a `Retry-After` header and a DaisyUI warning alert (
HTMX-friendly).

**Reverse proxy / Dokploy:** The `XForwardedHeaders` Ktor plugin is installed so that `request.origin.remoteHost`
resolves to the real client IP (via the `X-Forwarded-For` header set by Traefik). This works out of the box with
Dokploy's default Traefik setup.

**Scaling note:** Rate limiting is in-memory (per application instance). For a single-container deployment (typical
Dokploy setup) this is correct. If you scale to multiple replicas, each instance maintains its own buckets — meaning
effective limits multiply by the number of instances. For strict global limits across replicas, switch to a distributed
rate limiter (e.g. Bucket4j + Redis).

### Docker / Dokploy deployment

The app ships with a multi-stage `Dockerfile`:

- **Builder stage:** `gradle:8.13-jdk21` downloads dependencies and builds the fat JAR (skipping JOOQ generation — uses
  committed sources in `src/main/java/`).
- **Runtime stage:** `gcr.io/distroless/java21-debian12:nonroot` — minimal, secure image with only the JRE and the fat
  JAR.

To build and run locally:

```Terminal
docker build -t passgen:latest .
docker run -p 8080:8080 \
  -e SECRET_PASSGEN_DB-HOST=host.docker.internal \
  -e SECRET_PASSGEN_DB-USER=admin \
  -e SECRET_PASSGEN_DB-PASSWORD=your-password \
  passgen:latest
```

To deploy with Dokploy:

1. Point Dokploy to the repo — it builds the Docker image from the `Dockerfile` automatically (no pre-built JAR
   required).
2. The `XForwardedHeaders` plugin ensures rate limiting uses the real client IP from Traefik's `X-Forwarded-For` header.
3. Set the required environment variables in Dokploy's service config:
    - `SECRET_PASSGEN_DB-HOST` — PostgreSQL host (Dokploy internal service name or external)
    - `SECRET_PASSGEN_DB-USER` — Database user
    - `SECRET_PASSGEN_DB-PASSWORD` — Database password
    - `APP_HOST` — Bind address (default `0.0.0.0`, usually fine)
4. Health check: Traefik can use the `/health` endpoint (returns 200 OK).

**JOOQ class generation:** JOOQ sources are committed to `src/main/java/de/mw/generated/`. Every normal build (
`./gradlew build`) automatically regenerates them via testcontainers (requires Docker running locally). The Docker image
build skips JOOQ generation (`-x generateJooqClasses`) and uses the committed copies instead, avoiding Docker-in-Docker.
After changing DB migrations, just run `./gradlew build` and commit the updated generated files.

## Todo's

- Test pem and all key combinations
- Verify Ed25519 WebCrypto key creation (DER extraction, OpenSSH format round-trip)
- Also persist the key settings in local storage(restore on page load)
- make sure test coverage is good also for e2e testing
- Checkout how to move password generation to client here are some ideas:
    - list of hidden tags and take form there?
    - Alpine.js
    - Hyperscript?
    - Add local storage loading request to text area response(normal return + random wordlist to local storage)
        - hx-Ext json-enc
        - and from buttons and settings use local storage
- Change password amount slider so live and not only release(if the password is generated locally no need to only
  generate on
  release)
- See what we can clean up from the time when we still generated the passwords on the server(additional request
  handling, server side
  generation code, etc)
- Check if we can remove the "Centralized modal opening" and "Global guard: block HTMX share"
- Is all places were we reference the public key are referenced escaping correct(double check, this is user created, so
  will be malicious)?
- Do personal security audit of the key gen and check if messed up somewhere (make sure we are secure)
- Add go home button to expired shares page
- Add multi language support similar to marble game(start with DE and ENG)
- add required footer stuff(use marble game repo as inspiration) and add an about page with technical explanation and
  why
  this is secure. -> that explains the process
    - Password generation
    - How a share is stored
    - What is used for the Keygen
    - remove the report a bug/feedback link to github issue(later will add a real feedback process)
- host(passgen io/sh/...?)
- Add posthog(similar setup as marbles with different project)
    - Switch to pay as you go and set spending limits of 10 bucks per project
    - Generate new token and then setup project
- Cookie banner?(Similar as marbles)
- Add workos for logins
    - login functionality requires monthly fee(check how this works with payment provider)
    - Remove bottom Github and donation link and add imp
    - users can view/delete their own shares
    - Users can set the expiry time for their shares(UTC timestamp(max 1 year) default 7 days enterprice can set any
      timestamp or 0/null for infinite)
    - enterprise users can create subdomains like companyX.passgen....(checkout workos pricing for SSO to see how much
      enterprise would cost)
    - enterprise users can can invite other users to the subdomain(admin and users)
        - Admins can view/delete all subdomain shares
        - Normal users can just manage their own shares
- Pricing and payment setup(Base: free, User: 1Euro per month or 10 a year)
- auto delete of shares(cleanup process 7days pro and business users can set any number up to a year or unlimited(job
  that changes the status of a user also needs to cleanup))(could create a Scheduled Tasks in dokploy vs job/task in
  passgen)
- Add posthog feedback setup
- Add Keyboard navigation(small icon that reveals the keymap)
    - UI for this? mini icon with popup on click
    - Mac vs Linux vs Win
    - K vs P for main screen S for the share variant R for (re)gen
- Marketing: Checkout quota and Reddit for password sharing SFTP setup admin subreddit
- Check/test if rate limiting works with current deployment
- Log analytics? Check open source projects or just sentry(self eval)
- Optimize
    - click to response time?
        - Check where we can add loaders
        - Navigation? -> on click already add the highlighting
    - Loading times:
        - Introduce static html pages?
        - Build step to produce the pages?
        - checkout creating static html files for a CDN?
- Think about cleanup database records
    - Cronjob?
    - Database config?
- Extend share settings
    - Custom Expire date(hard coded for free(greyed out))
    - IP whitelisting(CIDR/Single address)
    - How often can a password be clicked?
        - Implement
        - Add remaining views to password page
- move "validAlgorithms" and other hardcoded strings unified place and get rid of magic strings
- General README.MD cleanup
    - Logo
    - Texts
- API for easy programmatic access
    - hx-Ext json-enc
    - Get a password with parameters
    - Create share and get link
    - Update README.MD
    - Add docs for API to page
- SSH connection to get a password?(Good practice not really useful here)

https://p.7mw.de/

## Support me :heart: :star: :money_with_wings:

If this project provided value, and you want to give something back, you can give the repo a star or support me, by
tipping me a coffee.

<a href="https://buymeacoffee.com/MartinWie" target="_blank"><img src="https://cdn.buymeacoffee.com/buttons/v2/default-blue.png" alt="Buy Me A Coffee" width="170"></a>
