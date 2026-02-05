import React, { useState, useEffect } from 'react';
import axios from 'axios';
import regeneratorRuntime from "regenerator-runtime";
import DataService from './data'; 

const BankAccountDetails = () => {
    const [accounts, setAccounts] = useState([]);

    useEffect(() => {
      const fetchAccounts = async () => {
        const dataService = new DataService();
        const response = await axios.get('http://localhost:8080/api/bankaccounts/');
        // const response = await dataService.getAccountDetails();
        setAccounts(response.data);
      };
          
      fetchAccounts();
    }, []);

  return (
    <>
    <div className='mwrapper'>
        <span className='mSpanHeading'>Account Details</span>
        <div className='mcontainer'>
                <table className='mtable'>
                    <thead>
                    <tr>
                        <th>Account Number</th>
                        <th>Type</th>
                        <th>Balance</th>
                    </tr>
                    </thead>
                    <tbody>
                    {accounts.map(account => (
                        <tr key={account.id}>
                        <td style={{width:"10rem"}}>{account.accountNumber}</td>
                        <td style={{width:"7rem"}}>{account.type}</td>
                        <td style={{width:"10rem"}}>{ new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(account.balance) }</td>
                        </tr>
                    ))}
                    </tbody>
                </table>
          </div>
    </div>
    </>
  );
};

export default BankAccountDetails;
