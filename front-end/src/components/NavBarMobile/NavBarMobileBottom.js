import React, {useState } from 'react'
import './NavBarMobile.css'
import { Link } from 'react-router-dom'
import { Key,ChatLeftText, FileEarmark, Share } from 'react-bootstrap-icons';

const NavBarMobileBottom = () => {
   
    return <header className="NavStyle">
        <nav>
            <ul className="nav__links">
                <li>
                    <Link className="LinkItems" to="/passgen">
                        PassGen <Key className="ButtonImage" />
                    </Link>
                </li>
                <li>
                    <Link className="LinkItems" to="/words">
                        Words <ChatLeftText className="ButtonImage" />
                    </Link>
                </li>
                <li>
                    <Link className="LinkItems" to="/keypair">
                        Keypair <FileEarmark className="ButtonImage" />
                    </Link>
                </li>
                <li>
                    <Link className="LinkItems" to="/send">
                        Send <Share className="ButtonImage" />
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