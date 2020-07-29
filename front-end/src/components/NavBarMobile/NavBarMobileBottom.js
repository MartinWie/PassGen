import React, {useState } from 'react'
import './NavBarMobile.css'
import { Link } from 'react-router-dom'
import { Key,ChatLeftText, FileEarmark, Share } from 'react-bootstrap-icons';

const NavBarMobileBottom = () => {
   
    return <header className="NavStyle__mobile">
        <nav className="nav__mobile__bottom">

                    <Link to="/passgen" className="nav__mobilelink nav__mobilelink--active">
                        <Key className="nav__icon" size={28} />
                        <span className="nav__mobiletext">PassGen</span>
                    </Link>
                    <Link to="/words" className="nav__mobilelink">
                        <ChatLeftText className="nav__icon" size={28} />
                        <span className="nav__mobiletext">Words</span>
                    </Link>
                    <Link to="/keypair" className="nav__mobilelink">
                        <FileEarmark className="nav__icon" size={28} />
                        <span className="nav__mobiletext">Keypair</span>
                    </Link>
                    <Link to="/send" className="nav__mobilelink">
                        <Share className="nav__icon" size={28} />
                        <span className="nav__mobiletext">Send</span>
                    </Link>
        </nav>
      </header>

      function contact() {
        window.location.href = 'https://www.linkedin.com/in/martin-wiechmann-2b5aa3151/';
          
      }
}
export default NavBarMobileBottom;