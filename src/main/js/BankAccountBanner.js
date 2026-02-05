import React, { useState, useEffect } from 'react';
import axios from 'axios';

const BankAccountBanner = () => {

  return (
    <div className='bOuterCard'>
	    <div style={{width:"30%"}}>
          <div className='bcard'>
		        <span className='bspan1'>Get $500 off</span>
          </div>
		  </div>
		  <div className='binnerCard' style={{ width:"60%"}}>
            <div>
                <span className='bspan2'>New Banco Sabadell checking customers</span>
            </div>
            <div>
                <span className='bspan3'>Open a Banco Sabadell Total Checking® account and set up direct deposit.</span>
            </div>
            <button className="btnOpenAccount">Open an account</button>
      </div>
	</div>
  );
};

export default BankAccountBanner;