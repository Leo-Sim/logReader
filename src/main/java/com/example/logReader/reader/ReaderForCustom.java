package com.example.logReader.reader;

/**
 * Create random log.
 * Read json files that have multiple values for each log.
 *
 *  [
 *      {
 *          "delimiter": "$value",
 *          "columns": {
 *           // All field and possible values are in "column". field name is 'key'
 *              "date": ["2020-11-10 12:00:00.123", "2020-12-11 11:00:00.234" ...],
 *              "eventId" ["12345", "67897" ...]
 *              .....
 *          },
 *          ....
 *      }
 *
 *  ]
 *
 * */
public class ReaderForCustom {
}
