import React, {useState } from 'react'
import logo from '../../Images/Logo/logo_size_invert_without_border.jpg'
import './NavBarMobile.css'
import { Link } from 'react-router-dom'

const NavBarMobileTop = () => {
   
    return <header className="NavStyle">
        <Link to="/">
            <img src={logo} className="logo" alt="logo" />
        </Link>
        <a className="cta"><button onClick={contact}>Contact</button></a>

      </header>

      function contact() {
        window.location.href = 'https://www.linkedin.com/in/martin-wiechmann-2b5aa3151/';
          
      }
}
export default NavBarMobileTop;