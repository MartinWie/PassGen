import React, {useState } from 'react'
import logo from '../../Images/Logo/logo_size_invert_without_border.jpg'
import './NavBar.css'
import { Link } from 'react-router-dom'

const NavBar = () => {
   
    return <header className="NavStyle">
        <img src={logo} className="logo" alt="logo" />
        <nav>
            <ul className="nav__links">
                <li>
                    <Link className="LinkItems" to="/passgen">PassGen</Link>
                </li>
                <li>
                    <Link className="LinkItems" to="/keypair">Keypair</Link>
                </li>
                <li>
                    <Link className="LinkItems" to="/send">Send</Link>
                </li>
            </ul>
      </nav>
      <a className="cta"><button onClick={contact}>Contact</button></a>

      </header>

      function contact() {
        window.location.href = 'https://www.linkedin.com/in/martin-wiechmann-2b5aa3151/';
          
      }
}
export default NavBar;