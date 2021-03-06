import React from 'react';
import{ BrowserRouter as Router, Switch, Route} from 'react-router-dom'
import { BrowserView, MobileView} from "react-device-detect";
import './App.css';
import PasswordGeneration from './components/PasswordGeneration/PasswordGeneration.js';
import NavBar from './components/NavBar/NavBar'
import NavBarMobileTop from './components/NavBarMobile/NavBarMobileTop'
import NavBarMobileBottom from './components/NavBarMobile/NavBarMobileBottom'
import Keypair from './components/Keypair/Keypair' 
import Send from './components/Send/Send'
import Words from './components/Words/Words'
import { ThemeProvider } from '@material-ui/core/styles';
import theme from './config/theme';

function App() {
  return <div>
    <ThemeProvider theme={theme}>
      <Router>
        <BrowserView>
          <NavBar />
        </BrowserView>
        <MobileView>
            <NavBarMobileTop />
        </MobileView>
        <div className="tools">
          <Switch>
            <Route exact path="/keypair" component={Keypair}/>
            <Route exact path="/send" component={Send} />
            <Route exact path="/words" component={Words} />
            <Route path="/" component={PasswordGeneration} />
          </Switch>
        </div>
        <MobileView>
          <NavBarMobileBottom />
        </MobileView>
      </Router>
    </ThemeProvider>
  </div>
}

export default App;

// cleanup / read all comments and improve code
// cleanup css : https://www.freecodecamp.org/news/css-naming-conventions-that-will-save-you-hours-of-debugging-35cea737d849/
// about page / impressum nicht vergessen ;)
// clear / work through warnings 
// add JS/react linter