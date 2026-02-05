'use strict';
//import Navbar from './Navbar';
//import './App.css';
import "bootstrap/dist/css/bootstrap.css";
import "font-awesome/css/font-awesome.min.css";
const React = require('react'); 
const ReactDOM = require('react-dom'); 
//const client = require('./client'); 
//import BankAccountDetails from './AccountDetails';
import './App.css';
import BankAccountNavigation from './BankAccountNavigation';
import BankAccountDetails from './BankAccountDetails';
import BankAccountQuickLinks from './BankAccountQuickLinks';
import BankAccountBanner from './BankAccountBanner';
import BankAccountTransactions from './BankAccountTransactions';
import BankAccountPayment from './BankAccountPayment';

import Navbar from './Navbar';
import Sidebar from './Sidebar';
import axios from 'axios';
class App extends React.Component { 

	constructor(props) {
		super(props);

		if (typeof window !== 'undefined') {
			window.addEventListener('error', function (e) {
			  console.error("Error: " + e.message + " in " + e.filename + " at line " + e.lineno + " column " + e.colno);
			  //const response = axios.post('http://localhost:8080/api/bankaccounts/logmessage?logmsg=', e.message);
			  const response = axios.post('https://basic-q5gx4rjkda-uc.a.run.app/api/bankaccounts/logmessage?logmsg=', e.message);
		  });
		  }
	}

	render() { 
		return (
			<>
			<Navbar/>
			<div className='hwrapper'>
				<div className='hsidebar'>
					<Sidebar/>
				</div>
				<div className='hcontainerWrapper'>
					<div className='hcontainerNavigation'>
						<BankAccountNavigation/>
					</div>
					<div className='hcontainer'>
						<div className='hcontainerBox1'>
							<div className='hcontainerBox11'>
								<BankAccountDetails/>
							</div>
							<div className='hcontainerBox12'>
								<BankAccountQuickLinks />
							</div>						
						</div>
						<div className='hcontainerBox2'>
							<BankAccountTransactions/>
						</div>
					</div>
					<div className='hcontainerFooter'>
						<div className='hcontainerFooterBox1'>
							<BankAccountPayment/>
						</div>
						<div className='hcontainerFooterBox2'>
							<BankAccountBanner/>
						</div>
					</div>
				</div>
			</div>
			</>
	
		)
	}
}

ReactDOM.render(
	<App />,
	document.getElementById('react')
)