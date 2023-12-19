import * as fs from 'fs';
import * as path from 'path';
import * as XLSX from 'xlsx';

type FileParams = {
    /** The path to the file */
    dirPath: string;
    /** Filename only */
    fileName: string;
};
/**
 * Delete a file from the given folder
 */
export const deleteDownload = (opts: FileParams) => {
    const { dirPath, fileName } = opts;
    fs.readdir(dirPath, (_, files) => {
        for (const file of files) {
            if (fileName && file === fileName) {
                fs.unlinkSync(path.join(dirPath, file));
            }
        }
    });
};

type XlsxParams = FileParams & {
    /** A list of names from sheets that should be parsed */
    sheets?: string[];
    /** Range of the Excel Sheet to test @example 'A1:D5'*/
    refs?: string;
};
/**
 * Converts a xlsx file to json @see https://docs.sheetjs.com/docs/api/utilities/array#array-output
 */
export const convertXlsxToJson = (opts: XlsxParams) => {
    const { refs, sheets, fileName, dirPath } = opts;
    const workbook = XLSX.readFile(path.join(dirPath, fileName), { sheets });
    return Object.entries(workbook.Sheets).map(([name, data]) => {
        if (refs) {
            data['!ref'] = refs;
        }
        return {
            sheet: name,
            data: XLSX.utils.sheet_to_json<any[]>(data, { header: 1, blankrows: false }),
        };
    });
};
