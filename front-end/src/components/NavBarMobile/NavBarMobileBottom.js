import React, {useState } from 'react'
import './NavBarMobile.css'
import { Link } from 'react-router-dom'
import { Key,ChatLeftText, FileEarmark, Share } from 'react-bootstrap-icons';

const NavBarMobileBottom = () => {
   
    return <header className="NavStyle__mobile">
        <nav className="nav__mobile__bottom">
            <ul className="nav__mobilelinks">
                <li className="nav__mobilelink nav__mobilelink--active">
                    <Link to="/passgen">
                        <Key className="nav__icon" />
                        <span className="nav__mobiletext">PassGen</span>
                    </Link>
                </li>
                <li className="nav__mobilelink">
                    <Link to="/words">
                        <ChatLeftText className="nav__icon" />
                        <span className="nav__mobiletext">Words</span>
                    </Link>
                </li>
                <li className="nav__mobilelink">
                    <Link to="/keypair">
                        <FileEarmark className="nav__icon" />
                        <span className="nav__mobiletext">Keypair</span>
                    </Link>
                </li>
                <li className="nav__mobilelink">
                    <Link to="/send">
                        <Share className="nav__icon" />
                        <span className="nav__mobiletext">Send</span>
                    </Link>
                </li>
            </ul>
        </nav>
      </header>

      function contact() {
        window.location.href = 'https://www.linkedin.com/in/martin-wiechmann-2b5aa3151/';
          
      }
}
export default NavBarMobileBottom;