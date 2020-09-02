import { createMuiTheme } from '@material-ui/core/styles';

export default createMuiTheme({
    palette: {
      text: {
        primary: '#edf0f1',
      },
      primary: {
        main: '#0088A9',
      },
      secondary: {
        main: '#edf0f1',
      }
    },
    typography:{
        allVariants: {
            color: ''
        }
    },
    shape:{
      borderRadius:20,
    }
  });
