import React, {useState } from 'react'
import './NavBarMobile.css'
import { Link } from 'react-router-dom'

const NavBarMobileBottom = () => {
   
    return <header className="NavStyle">
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
      </header>

      function contact() {
        window.location.href = 'https://www.linkedin.com/in/martin-wiechmann-2b5aa3151/';
          
      }
}
export default NavBarMobileBottom;