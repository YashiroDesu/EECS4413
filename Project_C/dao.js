const os      = require('os');
const path    = require('path');
const sqlite3 = require('sqlite3');

const dbfile  = '4413/pkg/sqlite/Models_R_US.db';
const dbpath  = path.join(os.homedir(), ...dbfile.split('/'));
const db      = new (sqlite3.verbose()).Database(dbpath);

const GET_ALL_PRODUCTS = 'SELECT * '
                       + 'FROM Category';

const BY_ID = " Where id = ?";

module.exports = {
  getCatalog(id, success, failure = console.log) {
    let statement = [GET_ALL_PRODUCTS];
    let value = id;
    if(id != null){
      statement.push(BY_ID);
    }
    db.all(statement.join(""), value, (err, rows) => {
    if (err == null) {
      success(rows);
    } else {
      failure(err);
    }
  });
  }
};
