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

- Implement share functionality
    - Come up with a proper target for the post(popup or toast or link below(probably this one))
    - copy the link to the clipboard
    - Page that has the option to reveal the password which then drops the password from the DB
- Add toggle for dark/light mode
    - Add toggle button
    - Store state for that
    - Also store the word length and the language(checkout localStorage or sessionStorage)
- Add donation(keep the service running) button and Github link to the project
- Add option to generate Public/Private Keypair in browser client(some sort of toggle)
    - Figure out how to generate the pair on the client
    - Text field for the public key use something like
      this -> https://cruip.com/auto-growing-textarea-with-tailwind-css/
- Generate a link where user generate a private key and the creator gets the public link
- Checkout how to move password generation to client
    - list of hidden tags and take form there?
    - Alpine.js
    - Hyperscript?
- add required footer stuff
- host
- Move parts into lib
    - HTMX code
    - HTML stuff
- Optimize
    - click to response time?
        - Check where we can add loaders
        - Navigation? -> on click already add the highlighting
    - Loading times:
        - Introduce static html pages?
        - Build step to produce the pages?
        - checkout creating static html files for a CDN?
- simple pw on ssh hostname
- Think about cleanup database records
    - Cronjob?
    - Database config?

https://p.7mw.de/

## Support me :heart: :star: :money_with_wings:

If this project provided value, and you want to give something back, you can give the repo a star or support me, by
tipping me a coffee.

<a href="https://buymeacoffee.com/MartinWie" target="_blank"><img src="https://cdn.buymeacoffee.com/buttons/v2/default-blue.png" alt="Buy Me A Coffee" width="170"></a>
