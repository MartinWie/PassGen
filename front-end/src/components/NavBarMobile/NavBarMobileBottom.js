import React, {useState } from 'react'
import './NavBarMobile.css'
import { Link } from 'react-router-dom'
import { Key,ChatLeftText, FileEarmark, Share } from 'react-bootstrap-icons';

const NavBarMobileBottom = () => {
   
    return <header className="NavStyle__mobile">
        <nav className="nav__mobile__bottom">
            <Link to="/passgen" className="nav__mobilelink">
                <Key size={23} />
                <span>PassGen</span>
            </Link>
            <Link to="/words" className="nav__mobilelink">
                <ChatLeftText size={23} />
                <span>Words</span>
            </Link>
            <Link to="/keypair" className="nav__mobilelink">
                <FileEarmark size={23} />
                <span>Keypair</span>
            </Link>
            <Link to="/send" className="nav__mobilelink">
                <Share size={23} />
                <span>Send</span>
            </Link>
        </nav>
      </header>

      function contact() {
        window.location.href = 'https://www.linkedin.com/in/martin-wiechmann-2b5aa3151/';
          
      }
}
export default NavBarMobileBottom;