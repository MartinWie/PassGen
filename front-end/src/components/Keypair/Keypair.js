import React, {useState} from 'react';
import {makeStyles} from '@material-ui/core/styles'
import theme from '../../config/theme';
import './Keypair.css'
import PassContainer from '../../hooks/PassContainer'
import NamingContainer from '../../hooks/NamingContainer'

const useStyles = makeStyles({
    notchedOutline: {borderColor: theme.palette.primary.main + " !important"},
});

function Keypair(){

    const classes = useStyles();
    const [nameState,setNameState] = useState("key")

    return <div className="toolsframe">
      <NamingContainer />
        
      <PassContainer title="Public key:" value="" />
      <PassContainer title="Private key:" hidden value="" />
    </div>
}

export default Keypair
