import React from 'react';
import{ BrowserRouter as Router, Switch, Route} from 'react-router-dom'
import './App.css';
import PasswordGeneration from './components/PasswordGeneration/PasswordGeneration.js';
import NavBar from './components/NavBar/NavBar'
import Keypair from './components/Keypair/Keypair' 
import Send from './components/Send/Send'

function App() {
  return <div>
    <Router>
        <NavBar />
        <div className="Tools">
          <Switch>
            <Route exact path="/keypair" component={Keypair}/>
            <Route exact path="/send" component={Send} />
            <Route path="/" component={PasswordGeneration} />
          </Switch>
        </div>
    </Router>
  </div>
}

export default App;
  
// about page / impressum nicht vergessen ;)
// Logo und Favicon nicht vergessen
