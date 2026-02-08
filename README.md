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

## Todo's

- Key Share modal "Security" icon and text alignemnt and copy button me design consistent
- After downloading the private key just show success and text box for the public key, but not the download button(less
  technical users get confused by the next download view. But when opening the link again the interface is good)
- Update "Click Generate to create a key pair" to something like "On device private key generation" with the smal infro
  icon when hovering short explain all 3 buttons and ondevice generation
- Rate-limiting / abuse prevention on share endpoints
- Label input on landing page share UI
- Share expiry/cleanup (expires_at)
- Security: Make PageSecurityContext.scriptNonce request-scoped
- CSP rule cleanup for production
- cleanup review codebase check for duplicated or old orphane logic and clean up
- make sure all test coverage is good also for e2e testing
- Checkout how to move password generation to client
    - list of hidden tags and take form there?
    - Alpine.js
    - Hyperscript?
    - Add local storage loading request to text area response(normal return + random wordlist to local storage)
        - hx-Ext json-enc
        - and from buttons and settings use local storage
        - Change slider so live and not only release(if the password is generated locally no need to only generate on
          release)
- See what we can clean up when we still generated the passwords on the server(additional request handling, server side
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
    - For docker image check out distroless(similar setup as marbles)
- Add Keyboard navigation(small icon that reveals the keymap)
    - UI for this? mini icon with popup on click
    - Mac vs Linux vs Win
    - K vs P for main screen S for the share variant R for (re)gen
- Add posthog(similar setup as marbles with different project)
- Cookie banner?(Similar as marbles)
- Add workos for logins
    - login functionality requires monthly fee(check how this works with payment provider)
    - Remove bottom Github and donation link and add imp
    - users can view/delete their own shares
    - enterprise users can create subdomains like companyX.passgen....(checkout workos pricing for SSO to see how much
      enterprise would cost)
    - enterprise users can can invite other users to the subdomain(admin and users)
        - Admins can view/delete all subdomain shares
        - Normal users can just manage their own shares
- auto delete of shares(cleanup process 7days pro and business users can set any number up to a year or unlimited(job
  that changes the status of a user also needs to cleanup))
- Add posthog feedback setup
- Marketing: Checkout quota and Reddit for password sharing SFTP setup admin subreddit
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
    - Custom Expire date
    - IP whitelisting(CIDR/Single address)
    - How often can a password be clicked?
        - Implement
        - Add remaining views to password page
- move "validAlgorithms" and other hardcoded strings inified place and get rid of magic strings
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
