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

- Add option to generate Public/Private Keypair in browser client(some sort of toggle)
    - Add option("Share" Pk with details on the process) feature similar to password share where we can share a
      link where the user can download a specific private key(
      will be generated on click and afterward the link only shows the public key for the user(the private key is only
      generated on the client(make sure the public key that is sent to the server is handled safely so a malicious
      actor can not inject html or js there)))
- Checkout how to move password generation to client
    - list of hidden tags and take form there?
    - Alpine.js
    - Hyperscript?
    - Add local storage loading request to text area response(normal return + random wordlist to local storage)
        - hx-Ext json-enc
        - and from buttons and settings use local storage
        - Change slider so live and not only release
- Fix bug: First loaded password is default and not the user config(potentially already rewrite to client side
  generation)(if still relevant)
- Do personal security audit of the key gen and check if messed up somewhere (make sure we are secure)
- Add go home button to expired shares page
- Make contrast/shadows on light theme better
- add required footer stuff(use marble game repo as inspiration) and add a about page with technical explanation and why
  this is secure. -> that explains the process
    - Password generation
    - How a share is stored
    - What is used for the Keygen
- security page in footer, private key on device generation + describe how password sharing is made save.
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
- Marketing: Checkout quota and Reddit for password sharing SFTP setup admin subreddit
- Add feedback option/page(look into new project for that, maybe posthog has something)
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
- Store selection state /reload it for crashes(always check for state file in current dir)

https://p.7mw.de/

## Support me :heart: :star: :money_with_wings:

If this project provided value, and you want to give something back, you can give the repo a star or support me, by
tipping me a coffee.

<a href="https://buymeacoffee.com/MartinWie" target="_blank"><img src="https://cdn.buymeacoffee.com/buttons/v2/default-blue.png" alt="Buy Me A Coffee" width="170"></a>
