import React, {useState} from 'react';
import './Keypair.css'
import PassContainer from '../../hooks/PassContainer'
import InputContainer from '../../hooks/InputContainer'

function Keypair(){

    const [nameState,setNameState] = useState("key")

    return <div className="toolsframe">
      <InputContainer outlinedName="Name for the key"/>
        
      <PassContainer title="Public key:" value="" />
      <PassContainer title="Private key:" hidden value="" />
    </div>
}

export default Keypair
