/**
 * @addtogroup CCNL-fwd
 * @{
 * @file ccnl-localrpc.h
 * @brief CCN-lite - local RPC processing logic
 *
 * @author Christian Tschudin <christian.tschudin@unibas.ch>
 *
 * @copyright (C) 2014-2018, Christian Tschudin, University of Basel
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

#ifndef CCNL_LOCALRPC_H
#define CCNL_LOCALRPC_H

#include "ccnl-relay.h"
#include "ccnl-face.h"


/**
 * @brief       Processing of Local RPC messages
 * 
 * @param[in] relay     pointer to current ccnl relay
 * @param[in] from      face on which the message was received
 * @param[in] buf       data which were received
 * @param[in] buflen   length of the received data
 *
 * @return      < 0 if no bytes consumed or error
 */
int
ccnl_localrpc_exec(struct ccnl_relay_s *relay, struct ccnl_face_s *from,
                   unsigned char **buf, int *buflen);

#endif

/** @} */
