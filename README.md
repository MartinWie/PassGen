# PassGen

![logo](https://github.com/MartinWie/PassGen/blob/master/logo.png)

A small tool to eliminate some personal pain points build with Kotlin + HTMX and Tailwind/franken-ui

### Install tooling:

Tailwind: for CSS generation
Franken-ui: for simpler base styling
Browser-sync: auto refresh browser on changes, for details checkout 'startServer.sh'
Entr: trigger action on file change, for details checkout 'startServer.sh'
Aenv: load app environment varibles form the aws parameter store

```Terminal
npm install -D tailwindcss@3
npm install -D @tailwindcss/typography
npm install postcss franken-ui@latest
npx franken-ui init -p
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
The script uses entr to trigger another script on every *.kt file change.
(entr will exit if new files are created, this is the reason for the loop, for details read the man page of entr)
The second script (start server) builds our CSS and runs the app server with gradle.
After a certain log stage is reached the script starts/refreshes a browser session with browser-sync to live update the
page.
If startServer script receives an user exit, we run a stop script and break out of the loop to exit the
buildAndReloadBrowserOnsave.sh script.

### Build and run the project

#### Setup local postgres

Build the Docker image: Open a terminal and navigate to the directory containing the Dockerfile_Postgres. Then run the following command:  

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

- get icons working
- Test if minified is smaller frankenUI is smaller
- Get them set in header function
- get base page with franke-UI setup and working
- get simple placeholder base page
- get logo done(check franken-ui icons)
- get favicons working


https://p.7mw.de/

## Support me :heart: :star: :money_with_wings:
If this project provided value, and you want to give something back, you can give the repo a star or support me, by tipping me a coffee.

<a href="https://buymeacoffee.com/MartinWie" target="_blank"><img src="https://cdn.buymeacoffee.com/buttons/v2/default-blue.png" alt="Buy Me A Coffee" width="170"></a>
